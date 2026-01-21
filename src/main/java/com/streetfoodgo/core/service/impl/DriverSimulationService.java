package com.streetfoodgo.core.service.impl;

import com.streetfoodgo.core.model.Order;
import com.streetfoodgo.core.model.OrderStatus;
import com.streetfoodgo.core.model.OrderType;
import com.streetfoodgo.core.repository.OrderRepository;
import com.streetfoodgo.core.service.OrderService;
import com.streetfoodgo.core.service.RoutingProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simulates driver movement for active DELIVERING orders using OSRM routes.
 * <p>
 * This acts as a "Mock Driver App", calling the main OrderService to update location.
 */
@Service
public class DriverSimulationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverSimulationService.class);

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final RoutingProxyService routingProxyService;

    // Cache active routes to avoid spamming OSRM: OrderID -> List of [Lon, Lat]
    private final Map<Long, List<List<Double>>> activeRoutes = new ConcurrentHashMap<>();
    // Track progress index for each order
    private final Map<Long, Integer> routeProgress = new ConcurrentHashMap<>();

    // Simulation speed: How many points to skip per tick (approx speed)
    private static final int POINTS_PER_TICK = 3;

    public DriverSimulationService(final OrderRepository orderRepository,
                                   final OrderService orderService,
                                   final RoutingProxyService routingProxyService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.routingProxyService = routingProxyService;
    }

    @Scheduled(fixedRate = 3000) // Update every 3 seconds
    @Transactional
    public void simulateDriverMovement() {
        // Find all orders that are currently being delivered
        List<Order> activeOrders = orderRepository.findAllByStatusAndOrderType(
                OrderStatus.DELIVERING, OrderType.DELIVERY);

        // Cleanup finished orders from cache
        cleanupCache(activeOrders);

        for (Order order : activeOrders) {
            try {
                updateOrderLocationWithType(order);
            } catch (Exception e) {
                LOGGER.error("Failed to simulate driver movement for order {}", order.getId(), e);
            }
        }
    }

    private void cleanupCache(List<Order> activeOrders) {
        List<Long> activeIds = activeOrders.stream().map(Order::getId).toList();
        // Remove routes for orders not in active list
        activeRoutes.keySet().removeIf(id -> !activeIds.contains(id));
        routeProgress.keySet().removeIf(id -> !activeIds.contains(id));
    }

    private void updateOrderLocationWithType(Order order) {
        // 1. Determine Start/End
        Double currentLat = order.getDriverLatitude();
        Double currentLon = order.getDriverLongitude();

        // Initialize start position at store if not set
        if (currentLat == null || currentLon == null) {
            currentLat = order.getStore().getLatitude();
            currentLon = order.getStore().getLongitude();

            // Initial save to set start point directly (before loop starts)
            order.setDriverLatitude(currentLat);
            order.setDriverLongitude(currentLon);
        }

        if (order.getDeliveryAddress() == null) return;
        Double targetLat = order.getDeliveryAddress().getLatitude();
        Double targetLon = order.getDeliveryAddress().getLongitude();
        if (targetLat == null || targetLon == null) return;

        // 2. Get Route (Cached or New)
        List<List<Double>> path = activeRoutes.get(order.getId());

        if (path == null) {
            // Fetch from OSRM
            try {
                RoutingProxyService.RouteWithGeometry route = routingProxyService.getRoute(
                        currentLat, currentLon, targetLat, targetLon
                );

                if (route.geometry() != null && route.geometry().coordinates() != null && !route.geometry().coordinates().isEmpty()) {
                    path = route.geometry().coordinates();
                    activeRoutes.put(order.getId(), path);
                    routeProgress.put(order.getId(), 0);
                    LOGGER.info("Fetched new OSRM route for Order {} with {} points", order.getId(), path.size());
                } else {
                    activeRoutes.put(order.getId(), java.util.Collections.emptyList());
                }
            } catch (Exception e) {
                LOGGER.error("OSRM fetch failed for order {}, using fallback", order.getId());
                activeRoutes.put(order.getId(), java.util.Collections.emptyList());
            }
        }

        // 3. Move along path
        if (path != null && !path.isEmpty()) {
            int currentIndex = routeProgress.getOrDefault(order.getId(), 0);
            int nextIndex = Math.min(currentIndex + POINTS_PER_TICK, path.size() - 1);

            if (nextIndex > currentIndex) {
                // GeoJSON is [Lon, Lat]
                List<Double> point = path.get(nextIndex);
                Double nextLon = point.get(0);
                Double nextLat = point.get(1);

                // --- CALL CENTRAL API ---
                orderService.updateDriverLocation(order.getId(), nextLat, nextLon);

                routeProgress.put(order.getId(), nextIndex);

                if (nextIndex == path.size() - 1) {
                   LOGGER.info("Order {} arrived at destination (simulation)", order.getId());
                }
            }
        }
    }
}
