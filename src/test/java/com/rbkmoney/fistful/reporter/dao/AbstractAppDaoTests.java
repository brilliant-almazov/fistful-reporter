package com.rbkmoney.fistful.reporter.dao;

import com.rbkmoney.TestContainers;
import com.rbkmoney.TestContainersBuilder;
import com.rbkmoney.fistful.reporter.FistfulReporterApplication;
import com.rbkmoney.fistful.reporter.utils.AbstractWithdrawalTestUtils;
import com.rbkmoney.fistful.reporter.utils.FistfulReporterTestPropertyValuesBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = FistfulReporterApplication.class, initializers = AbstractAppDaoTests.Initializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractAppDaoTests extends AbstractWithdrawalTestUtils {

    private static TestContainers testContainers = TestContainersBuilder.builder(false)
            .addPostgreSQLTestContainer()
            .build();

    @BeforeClass
    public static void beforeClass() {
        testContainers.startTestContainers();
    }

    @AfterClass
    public static void afterClass() {
        testContainers.stopTestContainers();
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            FistfulReporterTestPropertyValuesBuilder.build(testContainers).applyTo(configurableApplicationContext);
        }
    }
}
