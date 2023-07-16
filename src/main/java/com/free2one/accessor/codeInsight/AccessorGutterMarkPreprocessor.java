package com.free2one.accessor.codeInsight;

import com.free2one.accessor.settings.AccessorSettings;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.openapi.editor.GutterMarkPreprocessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import com.jetbrains.php.lang.findUsages.PhpGotoTargetRendererProvider;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

public class AccessorGutterMarkPreprocessor implements GutterMarkPreprocessor {

    @Override
    public @NotNull List<GutterMark> processMarkers(@NotNull List<GutterMark> list) {
        for (GutterMark mark : list) {
            if (mark instanceof LineMarkerInfo.LineMarkerGutterIconRenderer markerGutterIconRenderer && ((LineMarkerInfo.LineMarkerGutterIconRenderer) mark).getLineMarkerInfo().getNavigationHandler().getClass().getName().equals("com.jetbrains.php.lang.PhpLineMarkerProvider$PhpGutterIconNavigationHandler")
            ) {

                try {
                    Class<? extends LineMarkerInfo> lineMarker = markerGutterIconRenderer.getLineMarkerInfo().getClass();
                    java.lang.reflect.Field myNavigationHandler = lineMarker.getDeclaredField("myNavigationHandler");
                    myNavigationHandler.setAccessible(true);

                    Object myNavigationHandlerObj = myNavigationHandler.get(markerGutterIconRenderer.getLineMarkerInfo());
                    Class<?> myNavigationHandlerClass = myNavigationHandlerObj.getClass();
                    java.lang.reflect.Field myList = myNavigationHandlerClass.getDeclaredField("myList");
                    myList.setAccessible(true);
                    java.lang.reflect.Field myTitle = myNavigationHandlerClass.getDeclaredField("myTitle");
                    myTitle.setAccessible(true);
                    java.lang.reflect.Field myPinTitle = myNavigationHandlerClass.getDeclaredField("myPinTitle");
                    myPinTitle.setAccessible(true);

                    Object myListObj = myList.get(myNavigationHandlerObj);
                    Object myTitleObj = myTitle.get(myNavigationHandlerObj);
                    Object myPinTitleObj = myPinTitle.get(myNavigationHandlerObj);

                    myNavigationHandler.set(markerGutterIconRenderer.getLineMarkerInfo(), new PhpGutterIconNavigationHandler(
                            (Collection<? extends SmartPsiElementPointer<? extends PhpNamedElement>>) myListObj,
                            (String) myTitleObj,
                            (String) myPinTitleObj,
                            markerGutterIconRenderer.getLineMarkerInfo().getElement().getProject())
                    );

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

        }
        return list;
    }


    public static class PhpGutterIconNavigationHandler implements GutterIconNavigationHandler<PsiElement> {
        private final Collection<? extends SmartPsiElementPointer<? extends PhpNamedElement>> myList;
        private final @Nls String myTitle;
        private final @Nls String myPinTitle;
        private final Project project;

        PhpGutterIconNavigationHandler(Collection<? extends SmartPsiElementPointer<? extends PhpNamedElement>> list, @Nls String title, @Nls String pinTitle, Project project) {
            this.myList = list;
            this.myTitle = title;
            this.myPinTitle = pinTitle;
            this.project = project;
        }

        public void navigate(MouseEvent e, PsiElement __) {
            NavigatablePsiElement[] na = StreamEx.of(this.myList).map(SmartPsiElementPointer::getElement).nonNull().toArray(NavigatablePsiElement.EMPTY_NAVIGATABLE_ELEMENT_ARRAY);
            AccessorSettings settings = project.getService(AccessorSettings.class);
            NavigatablePsiElement[] naFinal = StreamEx.of(na).filter(navigatablePsiElement -> !settings.containSettingDirectories(navigatablePsiElement.getContainingFile().getVirtualFile().getPath())).toArray(NavigatablePsiElement.EMPTY_NAVIGATABLE_ELEMENT_ARRAY);
            PhpGotoTargetRendererProvider.PhpNamedElementPsiElementListCellRenderer renderer = new PhpGotoTargetRendererProvider.PhpNamedElementPsiElementListCellRenderer(false);
            PsiElementListNavigator.openTargets(e, naFinal, this.myTitle, this.myPinTitle, renderer);
        }
    }

}
