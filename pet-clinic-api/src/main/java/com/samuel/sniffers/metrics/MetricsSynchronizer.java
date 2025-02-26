//package com.samuel.sniffers.metrics;
//
//import com.samuel.sniffers.api.factory.LoggerFactory;
//import com.samuel.sniffers.api.logging.Logger;
//import com.samuel.sniffers.config.TokenConfig;
//import com.samuel.sniffers.repository.CustomerRepository;
//import com.samuel.sniffers.repository.ShoppingBasketRepository;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Component
//public class MetricsSynchronizer {
//
//    private static final Logger log = LoggerFactory.getLogger(MetricsSynchronizer.class);
//
//    private final PetShopMetrics metrics;
//    private final CustomerRepository customerRepository;
//    private final ShoppingBasketRepository basketRepository;
//    private final TokenConfig tokenConfig;
//
//    public MetricsSynchronizer(
//            PetShopMetrics metrics,
//            CustomerRepository customerRepository,
//            ShoppingBasketRepository basketRepository,
//            TokenConfig tokenConfig) {
//        this.metrics = metrics;
//        this.customerRepository = customerRepository;
//        this.basketRepository = basketRepository;
//        this.tokenConfig = tokenConfig;
//    }
//
//    @EventListener(ApplicationReadyEvent.class)
//    @Transactional(readOnly = true)
//    public void synchronizeMetricsOnStartup() {
//        log.info("Starting metrics synchronization on application startup...");
//
//        // Get all available tokens
//        List<String> allTokens = List.of(
//                tokenConfig.getAdminToken(),
//                tokenConfig.getCustomer1Token(),
//                tokenConfig.getCustomer2Token()
//        );
//
//        // Synchronize metrics for each token
//        for (String token : allTokens) {
//            log.info("Synchronizing metrics for token: {}", token);
//            synchronizeMetricsForToken(token);
//        }
//
//        log.info("Metrics synchronization completed successfully");
//    }
//
//    private void synchronizeMetricsForToken(String token) {
//        try {
//            // Count customers for this token
//            long customerCount = customerRepository.countByOwnerToken(token);
//            metrics.setTotalCustomers(token, (int) customerCount);
//            log.info("Set total customers metric for token {}: {}", token, customerCount);
//
////            // Count active baskets for this token
////            long activeBaskets = basketRepository.countByStatusNotAndOwnerToken("PROCESSED", token);
////            metrics.setActiveBaskets(token, (int) activeBaskets);
////            log.info("Set active baskets metric for token {}: {}", token, activeBaskets);
////
////            // Count processing baskets for this token
////            long processingBaskets = basketRepository.countByStatusAndOwnerToken("PROCESSING", token);
////            metrics.setProcessingBaskets(token, (int) processingBaskets);
////            log.info("Set processing baskets metric for token {}: {}", token, processingBaskets);
//        } catch (Exception e) {
//            log.error("Error synchronizing metrics for token {}: {}", token, e.getMessage(), e);
//        }
//    }
//}