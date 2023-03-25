package com.gleb.pycrunch;

import com.gleb.pycrunch.exceptionPreview.CapturedException;
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

public class NavigateToException {
    public void Go(PycrunchTestMetadata testByFqn, CapturedException exceptionForCurrentTestUnderCursor, PycrunchConnector _connector) {

        VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(exceptionForCurrentTestUnderCursor.filename);
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(_connector._project);
        Document document = FileDocumentManager.getInstance().getDocument(fileByPath);

        OpenFileDescriptor openFileDescriptor;
        openFileDescriptor = new OpenFileDescriptor(_connector._project, fileByPath, exceptionForCurrentTestUnderCursor.line_number - 1, -1, true);

        openFileDescriptor.navigate(true);
    }
}
