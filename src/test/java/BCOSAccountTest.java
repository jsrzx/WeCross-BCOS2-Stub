import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.bcos.account.BCOSAccount;
import java.io.IOException;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.junit.Test;

public class BCOSAccountTest {
    @Test
    public void transactionSignTest() throws IOException {
        Credentials credentials = GenCredential.create();
        BCOSAccount account = new BCOSAccount("test", "type", credentials);
        assertTrue(!account.getIdentity().isEmpty());
        assertEquals(account.getName(), "test");
        assertEquals(account.getType(), "type");
    }
}