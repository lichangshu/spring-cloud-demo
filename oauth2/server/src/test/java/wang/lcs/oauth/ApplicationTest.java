package wang.lcs.oauth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.Charset;
import java.util.Base64;

import javax.servlet.Filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@Rollback
public class ApplicationTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private Filter springSecurityFilterChain;
    private MockMvc mockMvc;
    private MockHttpSession session;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).addFilters(springSecurityFilterChain).build();
        this.session = new MockHttpSession();
    }

    @Test
    public void testIndex() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    public void testRpc() throws Exception {
        mockMvc.perform(get("/rpc/index.html"))//
                .andExpect(status().is(401));

        String encode = Base64.getEncoder().encodeToString("rpc-user:password".getBytes(Charset.forName("US-ASCII")));
        mockMvc.perform(get("/rpc/index.html").header("Authorization", "Basic " + encode))//
                .andExpect(status().isOk());
    }

    @Test
    public void testLogin() throws Exception {
        String req = "/oauth/authorize?response_type=code&client_id=demo-client&state=xyz&redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb";
        mockMvc.perform(get(req))//
                .andExpect(status().is(302))//
                .andExpect(header().stringValues("Location", "http://localhost/login"));

        mockMvc.perform(post("/login?username={0}&password={1}", "demo-user", "password").session(session))//
                .andExpect(status().is(302));

        String encode = Base64.getEncoder().encodeToString("demo-client:password".getBytes(Charset.forName("US-ASCII")));
        mockMvc.perform(get(req).header("Authorization", "Basic " + encode).session(session))//
                .andExpect(status().isOk());
    }
}
