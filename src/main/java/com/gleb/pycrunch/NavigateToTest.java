package com.gleb.pycrunch;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyFunction;

public class NavigateToTest {
    public void Go(PycrunchTestMetadata testByFqn, MyPycrunchConnector _connector) {

        VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(testByFqn.filename);
//        PsiDocumentManagerImpl.getInstance(_connector._project).getPsiFile(fileByPath)
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(_connector._project);
        Document document = FileDocumentManager.getInstance().getDocument(fileByPath);
        PyFile psiFile = (PyFile) documentManager.getPsiFile(document);
        String fqn = testByFqn.name;
        int text_offset = 0;
        boolean contains = fqn.contains("::");
        if (contains) {
            String[] split = fqn.split("::");
            PyClass topLevelClass = psiFile.findTopLevelClass(split[0]);
            if (topLevelClass != null) {
                PyFunction test_method2 = topLevelClass.findMethodByName(split[1], false, null);
                if (test_method2 != null) {
                    text_offset = test_method2.getTextOffset();
                }
            }
        } else {
            PsiElement psiElement = psiFile.findExportedName(fqn);
            text_offset = psiElement.getTextOffset();
        }
        OpenFileDescriptor openFileDescriptor;
        if (text_offset > 0) {
            openFileDescriptor = new OpenFileDescriptor(_connector._project, fileByPath, text_offset);
        }
        else {
            openFileDescriptor = new OpenFileDescriptor(_connector._project, fileByPath, 0, -1, true);
        }

        openFileDescriptor.navigate(true);
    }
}
