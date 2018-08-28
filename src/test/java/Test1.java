import gb.client.Main;
import gb.server.AuthService;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

public class Test1 {
    @Test
    public void test1() {
        try {
            Assert.assertEquals("nick1", AuthService.testConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2() {
        gb.server.Main server = new gb.server.Main();
        gb.client.Main client = new gb.client.Main();
        Assert.assertEquals("ok", gb.client.Controller.testClientConnection());
    }
}
