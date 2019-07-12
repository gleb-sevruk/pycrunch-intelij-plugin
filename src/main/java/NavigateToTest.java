import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyFile;

public class NavigateToTest {
    public void Go(PycrunchTestMetadata testByFqn, MyPycrunchConnector _connector) {

        // todo add line number in discovery metadata here!
        VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(testByFqn.filename);
//        PsiDocumentManagerImpl.getInstance(_connector._project).getPsiFile(fileByPath)
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(_connector._project);
        Document document = FileDocumentManager.getInstance().getDocument(fileByPath);
        PyFile psiFile = (PyFile) documentManager.getPsiFile(document);
        PsiElement psiElement = psiFile.findExportedName(testByFqn.name);
        OpenFileDescriptor openFileDescriptor;
        if (psiElement != null) {
            openFileDescriptor = new OpenFileDescriptor(_connector._project, fileByPath, psiElement.getTextOffset());
        }
        else {
            openFileDescriptor = new OpenFileDescriptor(_connector._project, fileByPath, 0, -1, true);
        }

        openFileDescriptor.navigate(true);
    }
}
