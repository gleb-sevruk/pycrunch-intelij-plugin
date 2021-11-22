//package com.jetbrains.python.testing;
//
//import com.gleb.pycrunch.ShowCoveringTestsAction;
//import com.gleb.pycrunch.actions.ConnectToCustomPycrunchEngineAction;
//import com.intellij.execution.ExecutionBundle;
//import com.intellij.execution.lineMarker.ExecutorAction;
//import com.intellij.execution.lineMarker.RunLineMarkerContributor;
//import com.intellij.execution.lineMarker.RunLineMarkerContributor.Info;
//import com.intellij.icons.AllIcons.RunConfigurations.TestState;
//import com.intellij.openapi.actionSystem.AnAction;
//import com.intellij.openapi.actionSystem.AnActionEvent;
//import com.intellij.psi.PsiElement;
//import com.intellij.psi.PsiNamedElement;
//import com.intellij.psi.impl.source.tree.LeafPsiElement;
//import com.intellij.util.ThreeState;
//import com.jetbrains.python.PyTokenTypes;
//import com.jetbrains.python.psi.PyClass;
//import com.jetbrains.python.psi.PyFunction;
//import com.jetbrains.python.psi.types.TypeEvalContext;
//import icons.PycrunchCachedIcons;
//import kotlin.Metadata;
//import kotlin.jvm.internal.Intrinsics;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.function.Function;
//
//public final class PycrunchLineMarkerContributor extends RunLineMarkerContributor {
//    @NotNull
//    public static final PycrunchLineMarkerContributor INSTANCE;
//
//    public static final Function<PsiElement, String> RUN_TEST_TOOLTIP_PROVIDER_PC = (it) -> {
//        return "RUN!!!!!! asdasd";
//    };
//
//    @Nullable
//    public Info getInfo(@NotNull PsiElement element) {
//        Intrinsics.checkNotNullParameter(element, "element");
//        if (element instanceof LeafPsiElement && !(Intrinsics.areEqual(((LeafPsiElement)element).getElementType(), PyTokenTypes.IDENTIFIER) ^ true)) {
//            PsiElement var10000 = ((LeafPsiElement)element).getParent();
//            if (var10000 == null) {
//                return null;
//            } else {
//                PsiElement testElement = var10000;
//                TypeEvalContext typeEvalContext = TypeEvalContext.codeAnalysis(((LeafPsiElement)element).getProject(), ((LeafPsiElement)element).getContainingFile());
//                if ((testElement instanceof PyClass || testElement instanceof PyFunction) && testElement instanceof PsiNamedElement) {
//                    ThreeState var10001 = ThreeState.UNSURE;
// TODO: is it possible to
//                    Intrinsics.checkNotNullExpressionValue(typeEvalContext, "typeEvalContext");
//                    if (PyTestsSharedKt.isTestElement(testElement, var10001, typeEvalContext)) {
//                        return new Info(new AnAction() {
//                            @Override
//                            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
//                                System.out.println("assdasdasd");
//                            }
//                        });
//                    }
//                }
//
//                return null;
//            }
//        } else {
//            return null;
//        }
//    }
//
//    private PycrunchLineMarkerContributor() {
//    }
//
//    static {
//        PycrunchLineMarkerContributor var0 = new PycrunchLineMarkerContributor();
//        INSTANCE = var0;
//    }
//}
