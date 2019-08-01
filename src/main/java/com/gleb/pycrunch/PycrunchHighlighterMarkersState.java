package com.gleb.pycrunch;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentManagerImpl;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

public class PycrunchHighlighterMarkersState {
    private static Hashtable<String, ArrayList<RangeHighlighterEx>> _highlighters_per_file = new Hashtable<>();

    public void invalidate_markers(Document document, Project project) {
        PycrunchConnector connector = ServiceManager.getService(PycrunchConnector.class);

        int myLine = 0;
//        System.out.println(myLine);

        String absolute_path = path_from_document(project, document);
        ArrayList<RangeHighlighterEx> all_highlighters_per_current_file;

        if (!_highlighters_per_file.containsKey(absolute_path)) {
            all_highlighters_per_current_file = new ArrayList<>();
            _highlighters_per_file.put(absolute_path, all_highlighters_per_current_file);
        } else {
            all_highlighters_per_current_file = _highlighters_per_file.get(absolute_path);
            all_highlighters_per_current_file.forEach(__ -> __.dispose());
        }
        SingleFileCombinedCoverage lines_covered = connector.GetCoveredLinesForFile(absolute_path);
        if (lines_covered != null) {


            MarkupModelEx markup = (MarkupModelEx) DocumentMarkupModel.forDocument(document, project, true);
            RangeHighlighterEx highlighter;
            HashMap<Integer, HashSet<String>> lines_hit_by_run = lines_covered._lines_hit_by_run;
            if (lines_hit_by_run != null) {
                lines_hit_by_run.keySet().forEach(__ -> addHighlighterForLine(__ - 1, connector.get_marker_color_for(absolute_path, __), markup, all_highlighters_per_current_file, absolute_path));
            }
            if (myLine >= 0) {
//            addHighlighterForLine(myLine, markup);
//            addHighlighterForLine(myLine+1, markup);
//            addHighlighterForLine(myLine+2, markup);
            } else {
                highlighter = null;
            }
//        PycrunchConnector connector = ServiceManager.getService(PycrunchConnector.class);
//        try {
//            connector.AttachToEngine(project);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        String txt= Messages.showInputDialog(project, "What is your name?", "Input your name", Messages.getQuestionIcon());
//        Messages.showMessageDialog(project, "Hello, " + txt + "!\n I am glad to see you.", "Information", Messages.getInformationIcon());
        }
    }

    @NotNull
    private String path_from_document(Project project, Document document) {
        PsiFile psiFile = PsiDocumentManagerImpl.getInstance(project).getPsiFile(document);
        return psiFile.getVirtualFile().getPath();
    }

    private void addHighlighterForLine(int myLine, String status, MarkupModelEx markup, ArrayList<RangeHighlighterEx> all_highlighters_per_current_file, String absolute_path) {
        RangeHighlighterEx highlighter;
        highlighter = markup.addPersistentLineHighlighter(myLine, 5001, (TextAttributes)null);
        if (highlighter != null) {
            highlighter.setGutterIconRenderer(new PyCrunchGutterIconRenderer(myLine, status, absolute_path));
            TextAttributes textAttributes = (TextAttributes) ObjectUtils.notNull(EditorColorsManager.getInstance().getGlobalScheme().getAttributes(CodeInsightColors.BOOKMARKS_ATTRIBUTES), new TextAttributes());
//            Color stripeColor = (Color)ObjectUtils.notNull(textAttributes.getErrorStripeColor(), new JBColor(0, 14408667));
//            highlighter.setErrorStripeMarkColor(stripeColor);
//            highlighter.setErrorStripeTooltip("abc");
            TextAttributes attributes = highlighter.getTextAttributes();
            if (attributes == null) {
                attributes = new TextAttributes();
            }

//            attributes.setBackgroundColor(new JBColor(0x35D5DB, 14408667));
            attributes.setBackgroundColor(textAttributes.getBackgroundColor());
            attributes.setForegroundColor(textAttributes.getForegroundColor());
            highlighter.setTextAttributes(attributes);
            all_highlighters_per_current_file.add(highlighter);
//            markup.fireAfterAdded(highlighter);

        }
    }
}
