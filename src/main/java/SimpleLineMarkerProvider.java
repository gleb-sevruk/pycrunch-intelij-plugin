import com.intellij.codeInsight.daemon.*;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralValue;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

public class SimpleLineMarkerProvider extends RelatedItemLineMarkerProvider {
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            Collection<? super RelatedItemLineMarkerInfo> result) {
        Project project = element.getProject();
        NavigationGutterIconBuilder<PsiElement> builder =
                NavigationGutterIconBuilder.create(new ImageIcon(getClass().getResource("/cherry-icon.png"))).
                        setTarget(element).
                        setTooltipText("Navigate to a simple property");
        result.add(builder.createLineMarkerInfo(element));
    }
}
