package com.gleb.pycrunch;

import com.gleb.pycrunch.shared.GlobalKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentManagerImpl;
import com.intellij.util.ObjectUtils;

import java.util.*;

public class PycrunchHighlighterMarkersState {
    private static Hashtable<String, ArrayList<RangeHighlighterEx>> _highlighters_per_file = new Hashtable<>();

    public void cleanup_everything(Project project) {
        PycrunchConnector connector = project.getService(PycrunchConnector.class);
        _highlighters_per_file.forEach(
                (s, markers) -> markers.forEach(__ -> __.dispose()));
        _highlighters_per_file = new Hashtable<>();
    }
    public Set<String> get_all_files_with_markers() {
        return _highlighters_per_file.keySet();
    }
    public void invalidate_markers(Document document, Project project) {
        PycrunchConnector connector = project.getService(PycrunchConnector.class);

        VirtualFile virtualFile = file_from_document(project, document);
        String absolute_path = virtualFile.getPath();
        ArrayList<RangeHighlighterEx> all_highlighters_per_current_file;
        if (_highlighters_per_file.containsKey(absolute_path)) {
//          There are old markers, clean all of them!
            all_highlighters_per_current_file = _highlighters_per_file.get(absolute_path);
            all_highlighters_per_current_file.forEach(__ -> __.dispose());
            all_highlighters_per_current_file.removeIf(__ -> true);
        } else {
            all_highlighters_per_current_file = new ArrayList<>();
            _highlighters_per_file.put(absolute_path, all_highlighters_per_current_file);
        }

        cleanup_stale_or_renamed_markers(virtualFile, absolute_path);

//        This is for tracking file renames.
//        Virtual Document will contain old filename in metadata even when file already renamed on disk
        virtualFile.putUserData(GlobalKeys.DOCUMENT_PATH_KEY, absolute_path);

        SingleFileCombinedCoverage lines_covered = connector.GetCoveredLinesForFile(absolute_path);
        if (lines_covered == null) {
//            System.out.println("maybe path cannot be resolved: " + absolute_path);
            return;
        }

        MarkupModelEx markup = (MarkupModelEx) DocumentMarkupModel.forDocument(document, project, true);
        HashMap<Integer, HashSet<String>> lines_hit_by_run = lines_covered._lines_hit_by_run;
        var exceptions_in_current_file = lines_covered._exceptions;

        Set<Integer> line_numbers = lines_hit_by_run.keySet();
        line_numbers.forEach(
                        __ -> addHighlighterForLine(
                                __ - 1,
                                connector.get_marker_color_for(absolute_path, __, exceptions_in_current_file),
                                markup,
                                all_highlighters_per_current_file,
                                absolute_path,
                                project)
                );

//            Process lines that are not in coverage but in exceptions
        exceptions_in_current_file.stream()
                .filter(lineNumber -> !line_numbers.contains(lineNumber))
                .forEach(lineNumber -> {
                    addHighlighterForLine(
                            lineNumber - 1,
                            connector.get_marker_color_for(absolute_path, lineNumber, exceptions_in_current_file),
                            markup,
                            all_highlighters_per_current_file,
                            absolute_path,
                            project);
                });
    }

    private void cleanup_stale_or_renamed_markers(VirtualFile virtualFile, String new_path) {

        Object userData = virtualFile.getUserData(GlobalKeys.DOCUMENT_PATH_KEY);
        String previous_filename = (String) userData;
        if (previous_filename == null) {
            return;
        }

//        System.out.println("user data not null, prev value:" + previous_filename);

        if (previous_filename.equals(new_path)) {
            // not renamed
            return;
        }
        // file was renamed, clean up old markers, otherwise they will stack in gutter in multiple columns
        if (_highlighters_per_file.containsKey(previous_filename)) {
            ArrayList<RangeHighlighterEx> markers = _highlighters_per_file.get(previous_filename);
            markers.forEach(__ -> __.dispose());
            _highlighters_per_file.remove(previous_filename);
        }

    }

    private VirtualFile file_from_document(Project project, Document document) {
        PsiFile psiFile = PsiDocumentManagerImpl.getInstance(project).getPsiFile(document);
        return psiFile.getVirtualFile();
    }

    private void addHighlighterForLine(int myLine, String status, MarkupModelEx markup, ArrayList<RangeHighlighterEx> all_highlighters_per_current_file, String absolute_path, Project project) {
        RangeHighlighterEx highlighter;
        highlighter = markup.addPersistentLineHighlighter(myLine, 5001, (TextAttributes)null);
        if (highlighter != null) {
            highlighter.setGutterIconRenderer(new PyCrunchGutterIconRenderer(myLine, status, absolute_path, project));
            highlighter.setAfterEndOfLine(false);
            all_highlighters_per_current_file.add(highlighter);
//            markup.fireAfterAdded(highlighter);

        }
    }
}
