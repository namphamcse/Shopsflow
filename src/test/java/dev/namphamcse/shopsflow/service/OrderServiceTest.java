package dev.namphamcse.shopsflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.namphamcse.shopsflow.dto.response.OrderResponse;
import dev.namphamcse.shopsflow.entity.CartItem;
import dev.namphamcse.shopsflow.entity.Order;
import dev.namphamcse.shopsflow.entity.Product;
import dev.namphamcse.shopsflow.entity.User;
import dev.namphamcse.shopsflow.entity.enums.OrderStatus;
import dev.namphamcse.shopsflow.entity.enums.Role;
import dev.namphamcse.shopsflow.exception.BusinessRuleViolationException;
import dev.namphamcse.shopsflow.exception.ResourceNotFoundException;
import dev.namphamcse.shopsflow.repository.CartItemRepository;
import dev.namphamcse.shopsflow.repository.OrderRepository;
import dev.namphamcse.shopsflow.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepo;
    @Mock
    CartItemRepository cartItemRepo;
    @Mock
    ProductRepository productRepo;

    @InjectMocks
    OrderService orderService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User("Nam", "n@x.com", "pw");
        user.setId(1L);
        user.setRole(Role.USER);

        product = new Product("Book", "desc", new BigDecimal("10"), null, 5);
        product.setId(100L);
    }


    @Test
    void placeOrder_throws_whenCartIsEmpty() {
        when(cartItemRepo.findByUser(user)).thenReturn(List.of());

        assertThrows(BusinessRuleViolationException.class,
                () -> orderService.placeOrder(user));

        verify(orderRepo, never()).save(any());
        verify(productRepo, never()).save(any());
        verify(cartItemRepo, never()).deleteByUser(any());
    }

    @Test
    void placeOrder_throws_whenInsufficientStock() {
        CartItem item = new CartItem(user, product, 99); // stock is 5
        when(cartItemRepo.findByUser(user)).thenReturn(List.of(item));

        assertThrows(BusinessRuleViolationException.class,
                () -> orderService.placeOrder(user));

        verify(orderRepo, never()).save(any());
        verify(productRepo, never()).save(any());
        verify(cartItemRepo, never()).deleteByUser(any());
    }

    @Test
    void placeOrder_decrementsStock_savesOrder_clearsCart_onHappyPath() {
        CartItem item = new CartItem(user, product, 2);
        when(cartItemRepo.findByUser(user)).thenReturn(List.of(item));
        when(orderRepo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.placeOrder(user);

        assertEquals(3, product.getStockQuantity()); // 5 - 2
        verify(productRepo).save(product);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepo).save(captor.capture());
        Order saved = captor.getValue();
        assertEquals(user, saved.getUser());
        assertEquals(OrderStatus.PENDING, saved.getStatus());
        assertEquals(new BigDecimal("20"), saved.getTotalAmount()); // 10 * 2
        assertEquals(1, saved.getItems().size());

        verify(cartItemRepo).deleteByUser(user);

        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("20"), response.getTotalAmount());
    }

    @Test
    void placeOrder_sumsTotalAcrossMultipleItems() {
        Product other = new Product("Pen", "desc", new BigDecimal("3"), null, 10);
        other.setId(200L);

        CartItem item1 = new CartItem(user, product, 2); // 10 * 2 = 20
        CartItem item2 = new CartItem(user, other, 4);   //  3 * 4 = 12

        when(cartItemRepo.findByUser(user)).thenReturn(List.of(item1, item2));
        when(orderRepo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder(user);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepo).save(captor.capture());
        assertEquals(new BigDecimal("32"), captor.getValue().getTotalAmount());
        assertEquals(2, captor.getValue().getItems().size());

        verify(productRepo).save(product);
        verify(productRepo).save(other);
        assertEquals(3, product.getStockQuantity());
        assertEquals(6, other.getStockQuantity());
    }


    @Test
    void getUserOrders_returnsMappedList() {
        Order order = new Order(user, new BigDecimal("20"));
        order.setId(7L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepo.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(order));

        List<OrderResponse> result = orderService.getUserOrders(user);

        assertEquals(1, result.size());
        assertEquals(7L, result.get(0).getId());
        verify(orderRepo).findByUserOrderByCreatedAtDesc(user);
    }


    @Test
    void getOrderById_throws_whenNotFound() {
        when(orderRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderById(user, 999L));
    }

    @Test
    void getOrderById_throwsNotFound_whenNonAdminViewsAnotherUsersOrder() {
        User otherUser = new User("Other", "o@x.com", "pw");
        otherUser.setId(2L);

        Order order = new Order(otherUser, new BigDecimal("20"));
        order.setId(7L);

        when(orderRepo.findById(7L)).thenReturn(Optional.of(order));

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderById(user, 7L));
    }

    @Test
    void getOrderById_returnsOrder_whenOwner() {
        Order order = new Order(user, new BigDecimal("20"));
        order.setId(7L);

        when(orderRepo.findById(7L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(user, 7L);
        assertEquals(7L, response.getId());
    }

    @Test
    void getOrderById_returnsOrder_whenAdminViewsAnotherUsersOrder() {
        User admin = new User("Admin", "a@x.com", "pw");
        admin.setId(99L);
        admin.setRole(Role.ADMIN);

        Order order = new Order(user, new BigDecimal("20"));
        order.setId(7L);

        when(orderRepo.findById(7L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(admin, 7L);
        assertEquals(7L, response.getId());
    }


    @Test
    void updateOrderStatus_throws_whenNotFound() {
        when(orderRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.updateOrderStatus(999L, OrderStatus.SHIPPED));
    }

    @Test
    void updateOrderStatus_updatesStatus_whenFound() {
        Order order = new Order(user, new BigDecimal("20"));
        order.setId(7L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepo.findById(7L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.updateOrderStatus(7L, OrderStatus.SHIPPED);

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        assertEquals(OrderStatus.SHIPPED, response.getStatus());
    }


    @Test
    void findAllOrders_returnsMappedList() {
        Order o1 = new Order(user, new BigDecimal("20"));
        o1.setId(1L);
        Order o2 = new Order(user, new BigDecimal("5"));
        o2.setId(2L);

        when(orderRepo.findAll()).thenReturn(List.of(o1, o2));

        List<OrderResponse> result = orderService.findAllOrders();

        assertEquals(2, result.size());
        verify(orderRepo).findAll();
    }
}
