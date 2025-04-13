package com.prishedko;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupListener implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(StartupListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("=== School Monitoring System webapp started successfully ===");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("=== School Monitoring System webapp is shutting down ===");
    }
}
