package tech.reinders.kotlin.databasemigrator.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MachineUtilTest {
    private MachineUtil util;

    @BeforeEach
    void beforeEach() {
        this.util = MachineUtil.INSTANCE;
    }

    @Test
    void hostName() throws UnknownHostException {
        assertEquals(InetAddress.getLocalHost().getHostName(), this.util.hostName());
    }

    //Manually tested with Activity Monitor, it checks out
    @Test
    void pid() {
        final RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        final String ip = bean.getName().split("@")[0];
        assertEquals(ip, util.pid());
    }
}
