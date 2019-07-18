import junit.framework.TestCase;

public class SandboxTests extends TestCase {
    public void test_SplitTest() {
        String fqn = "Class2::test_method";
        boolean contains = fqn.contains("::");
        if (contains) {
            String[] split = fqn.split("::");
            System.out.println("xxx");
        }
    }
}
