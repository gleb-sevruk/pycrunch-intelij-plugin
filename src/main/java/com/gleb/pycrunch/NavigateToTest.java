package com.gleb.pycrunch;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;

public class NavigateToTest {
    public void Go(PycrunchTestMetadata testByFqn, PycrunchConnector _connector) {

        VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(testByFqn.filename);
        if (fileByPath == null) {
            return;
        }

        int text_offset = ReadAction.compute(() -> {
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(_connector._project);
            Document document = FileDocumentManager.getInstance().getDocument(fileByPath);
            if (document == null) {
                return 0;
            }
            PyFile psiFile = (PyFile) documentManager.getPsiFile(document);
            if (psiFile == null) {
                return 0;
            }
            String fqn = testByFqn.name;
            int offset = 0;
            boolean contains = fqn.contains("::");
            if (contains) {
                String[] split = fqn.split("::");
                PyClass topLevelClass = psiFile.findTopLevelClass(split[0]);
                if (topLevelClass != null) {
                    PyFunction test_method2 = topLevelClass.findMethodByName(split[1], false, null);
                    if (test_method2 != null) {
                        offset = test_method2.getTextOffset();
                    }
                }
            } else {
                PsiElement psiElement = psiFile.findExportedName(fqn);
                if (psiElement != null) {
                    offset = psiElement.getTextOffset();
                }
            }
            return offset;
        });

        OpenFileDescriptor openFileDescriptor;
        if (text_offset > 0) {
            openFileDescriptor = new OpenFileDescriptor(_connector._project, fileByPath, text_offset);
        } else {
            openFileDescriptor = new OpenFileDescriptor(_connector._project, fileByPath, 0, -1, true);
        }

        openFileDescriptor.navigate(true);
    }
}
