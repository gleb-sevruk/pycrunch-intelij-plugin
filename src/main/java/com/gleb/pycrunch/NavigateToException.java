package com.gleb.pycrunch;

import com.gleb.pycrunch.exceptionPreview.CapturedException;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

public class NavigateToException {
    public void Go(PycrunchTestMetadata testByFqn, CapturedException exceptionForCurrentTestUnderCursor, PycrunchConnector _connector) {

        VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(exceptionForCurrentTestUnderCursor.filename);
        if (fileByPath == null) {
            return;
        }

        OpenFileDescriptor openFileDescriptor;
        openFileDescriptor = new OpenFileDescriptor(_connector._project, fileByPath, exceptionForCurrentTestUnderCursor.line_number - 1, -1, true);

        openFileDescriptor.navigate(true);
    }
}
