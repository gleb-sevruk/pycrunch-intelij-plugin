import com.intellij.ide.bookmarks.Bookmark;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
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
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentManagerImpl;
import com.intellij.ui.JBColor;
import com.intellij.util.ObjectUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Set;

public class MyPyHighlighter extends  AnAction {
    private static ArrayList<RangeHighlighterEx> _highlighters = new ArrayList<>()   ;

    // If you register the action from Java code, this constructor is used to set the menu item name
    // (optionally, you can specify the menu description and an icon to display next to the menu item).
    // You can omit this constructor when registering the action in the plugin.xml file.
    public MyPyHighlighter() {
        // Set the menu item name.
        super("Highlight Some _Lines");
        // Set the menu item name, description and icon.
        // super("Text _Boxes","Item description",IconLoader.getIcon("/Mypackage/icon.png"));
    }

    public void actionPerformed(AnActionEvent event) {
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        int myLine = editor.getCaretModel().getLogicalPosition().line;
        System.out.println(myLine);

        Project project = event.getData(PlatformDataKeys.PROJECT);
        Document document = editor.getDocument();
        PsiFile psiFile = PsiDocumentManagerImpl.getInstance(project).getPsiFile(document);
        String absolute_path = psiFile.getVirtualFile().getPath();

        MyPycrunchConnector connector = ServiceManager.getService(MyPycrunchConnector.class);
        Set<Integer> lines_covered = connector.GetCoveredLineForFile(absolute_path);
        MarkupModelEx markup = (MarkupModelEx) DocumentMarkupModel.forDocument(document, project, true);
        RangeHighlighterEx highlighter;
        lines_covered.forEach(__ -> addHighlighterForLine(__ - 1, markup));
        if (myLine >= 0) {
//            addHighlighterForLine(myLine, markup);
//            addHighlighterForLine(myLine+1, markup);
//            addHighlighterForLine(myLine+2, markup);
        } else {
            highlighter = null;
        }
//        MyPycrunchConnector connector = ServiceManager.getService(MyPycrunchConnector.class);
//        try {
//            connector.CollectDiagnostics(project);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        String txt= Messages.showInputDialog(project, "What is your name?", "Input your name", Messages.getQuestionIcon());
//        Messages.showMessageDialog(project, "Hello, " + txt + "!\n I am glad to see you.", "Information", Messages.getInformationIcon());
    }

    private void addHighlighterForLine(int myLine, MarkupModelEx markup) {
        RangeHighlighterEx highlighter;
        highlighter = markup.addPersistentLineHighlighter(myLine, 6001, (TextAttributes)null);
        if (highlighter != null) {
            highlighter.setGutterIconRenderer(new PyCrunchGutterIconRenderer(myLine));
            TextAttributes textAttributes = (TextAttributes) ObjectUtils.notNull(EditorColorsManager.getInstance().getGlobalScheme().getAttributes(CodeInsightColors.BOOKMARKS_ATTRIBUTES), new TextAttributes());
            Color stripeColor = (Color)ObjectUtils.notNull(textAttributes.getErrorStripeColor(), new JBColor(0, 14408667));
            highlighter.setErrorStripeMarkColor(stripeColor);
            highlighter.setErrorStripeTooltip("abc");
            TextAttributes attributes = highlighter.getTextAttributes();
            if (attributes == null) {
                attributes = new TextAttributes();
            }

            attributes.setBackgroundColor(new JBColor(0x35D5DB, 14408667));
            attributes.setForegroundColor(textAttributes.getForegroundColor());
            highlighter.setTextAttributes(attributes);
            _highlighters.add(highlighter);
            markup.fireAfterAdded(highlighter);

        }
    }
}


