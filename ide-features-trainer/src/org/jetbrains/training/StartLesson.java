package org.jetbrains.training;

import com.intellij.ide.FileEditorProvider;
import com.intellij.ide.scratch.ScratchpadManager;
import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorMouseAdapter;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.impl.EditorLastActionTrackerImpl;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jdesktop.swingx.action.ActionManager;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.graphics.DetailPanel;

import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 17/12/14.
 */
public class StartLesson extends AnAction {

    DetailPanel infoPanel;
    Balloon balloon;

    public void actionPerformed(final AnActionEvent e) {


        try {
            final VirtualFile vf;
            vf = ScratchpadManager.getInstance(e.getProject()).createScratchFile(Language.findLanguageByID("JAVA"));
            //TODO: Rename as a lesson name
            vf.rename(this, "test1.java");

            OpenFileDescriptor descriptor = new OpenFileDescriptor(e.getProject(), vf);
            final Editor editor = FileEditorManager.getInstance(e.getProject()).openTextEditor(descriptor, true);
            final Document document = editor.getDocument();

//            Editor myEditor = FileEditorManager.getInstance();



            InputStream is = this.getClass().getResourceAsStream("JavaLessonExample2.java");
            final String target = new Scanner(is).useDelimiter("\\Z").next();

            final Course course = new Course();
            final Lesson lesson = course.giveNotPassedLesson();
            if (lesson == null) {
                //TODO: add some handler here
                return;
            }
//MAIN LESSON CONTROLLER STARTS HERE--------------------------------


            showInfoPanel(editor);

            //kill all listeners
            MouseListener[] mouseListeners = editor.getContentComponent().getMouseListeners();
            for (MouseListener mouseListener : mouseListeners) {
                editor.getContentComponent().removeMouseListener(mouseListener);
            }

            //kill all listeners
            MouseMotionListener[] mouseMotionListeners = editor.getContentComponent().getMouseMotionListeners();
            for (MouseMotionListener mouseMotionListener : mouseMotionListeners) {
                editor.getContentComponent().removeMouseMotionListener(mouseMotionListener);
            }

            //muteButtonListeners as in find

            LessonProcessor.process(lesson, editor, e, document, target, infoPanel);



//MAIN LESSON CONTROLLER ENDS HERE----------------------------------

        } catch (BadLessonException ble) {
            ble.printStackTrace();
        } catch (BadCourseException bce) {
            bce.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        }
    }


    private void showInfoPanel(Editor editor) {
        final Dimension dimension = new Dimension(500, 60);

        final IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(editor.getProject());
        infoPanel = new DetailPanel(dimension);
        RelativePoint location = computeLocation(ideFrame, dimension);
        Rectangle infoBounds = new Rectangle((int) location.getPoint().getX(), (int) location.getPoint().getY(), (int) dimension.getWidth(),(int) dimension.getHeight());

        BalloonBuilder balloonBuilder = JBPopupFactory.getInstance().createBalloonBuilder(infoPanel);
        balloonBuilder.setHideOnClickOutside(false)
                .setCloseButtonEnabled(false)
                .setHideOnKeyOutside(false)
                .setBorderColor(new Color(0, 0, 0, 0))
                .setDialogMode(false)
                .setHideOnFrameResize(false)
                .setFillColor(new Color(0, 0, 0, 0))
                .setHideOnAction(false);

        balloon = balloonBuilder.createBalloon();
        balloon.setBounds(infoBounds);

        balloon.show(location, Balloon.Position.above);
    }

    private RelativePoint computeLocation(IdeFrame ideFrame, Dimension dimension){
        int statusBarHeight = ideFrame.getStatusBar().getComponent().getHeight();
        Rectangle visibleRect = ideFrame.getComponent().getVisibleRect();
        Point point = new Point(visibleRect.x + (visibleRect.width - dimension.width) / 2, visibleRect.y + visibleRect.height - dimension.height - statusBarHeight - 20);
        return new RelativePoint(ideFrame.getComponent(), point);
    }

    @Nullable
    private VirtualFile createFile(final Project project) throws IOException {
        final String fileName = "JavaLessonExample.java";


        Module[] modules = ModuleManager.getInstance(project).getModules();
        final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(modules[0]).getSourceRoots();

        InputStream is = this.getClass().getResourceAsStream(fileName);
        //final String content = new Scanner(is).useDelimiter("\\Z").next();
        final String content = "";

        /*
        Creating new file in Project view;
        copying content from initial file "fileName"
         */

        return ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>() {
            @Override
            @Nullable
            public VirtualFile compute() {
                try {
                    VirtualFile vFile = sourceRoots[0].createChildData(this, fileName);
                    VfsUtil.saveText(vFile, content);
                    return vFile;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });

    }
}