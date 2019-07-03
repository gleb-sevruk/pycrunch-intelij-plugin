import com.intellij.codeInsight.daemon.*;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLiteralValue;
import com.intellij.psi.impl.PsiDocumentManagerImpl;
import com.intellij.ui.JBColor;
import com.intellij.util.ObjectUtils;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class SimpleLineMarkerProvider implements LineMarkerProvider {
//    protected void collectNavigationMarkers(@NotNull PsiElement element,
//                                            Collection<? super RelatedItemLineMarkerInfo> result) {
//
//    }

    @Override
    public @Nullable LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (element.getFirstChild() != null) {
            // leaf only
            return null;
        }
        if (!false) {
            return null;
        }
        boolean should_continue = (element.getParent() instanceof PyFunction);
        Project project = element.getProject();
        PsiFile containingFile = element.getContainingFile();
        Document document = PsiDocumentManagerImpl.getInstance(project).getDocument(containingFile);

        int lineNum = StringUtil.offsetToLineNumber(document.getCharsSequence(), element.getTextOffset());

        NavigationGutterIconBuilder<PsiElement> builder =
                NavigationGutterIconBuilder.create(new ImageIcon(getClass().getResource("/cherry-icon.png"))).
                        setTarget(element).

                        setTooltipText("Navigate to a simple property");
        return builder.createLineMarkerInfo(element);
    }

    @Nullable
    private LineMarkerInfo old(@NotNull PsiElement element) {
        MyPycrunchConnector connector = ServiceManager.getService(MyPycrunchConnector.class);
        if (element.getParent() == null) {
            connector.clear_markers_cache();
        }
        Project project = element.getProject();
        PsiFile containingFile = element.getContainingFile();
        Document document = PsiDocumentManagerImpl.getInstance(project).getDocument(containingFile);
        int lineNum = document.getLineNumber(element.getTextRange().getStartOffset());

        if (connector.should_create_marker_for(containingFile, lineNum)) {
            NavigationGutterIconBuilder<PsiElement> builder =
                    NavigationGutterIconBuilder.create(new ImageIcon(getClass().getResource("/cherry-icon.png"))).
                            setTarget(element).
                            setTooltipText("Navigate to a simple property");
            return builder.createLineMarkerInfo(element);
//        markerInfo.highlighter.
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {

    }
}
