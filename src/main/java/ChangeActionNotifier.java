import com.intellij.util.messages.Topic;

public interface ChangeActionNotifier {

    Topic<ChangeActionNotifier> CHANGE_ACTION_TOPIC = Topic.create("pycrunch.event", ChangeActionNotifier.class);

    void beforeAction(String context);
    void afterAction(String context);
}