// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jdom.Element
import training.commands.Command
import training.commands.CommandFactory
import training.commands.ExecutionList
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

object LessonProcessor {

  val LOG = Logger.getInstance(LessonProcessor::class.java)

  @get:org.jetbrains.annotations.TestOnly
  var currentExecutionList: ExecutionList? = null
    private set

  fun process(project: Project, lesson: XmlLesson, editor: Editor) {

    val myQueueOfElements = createQueueOfCommands(lesson.scenario.root)

    //Initialize lesson in the editor
    LessonManager.instance.initLesson(editor, lesson)

    //Prepare environment before execution
    with(ExecutionList(myQueueOfElements, lesson, project)) {
      currentExecutionList = this
      CommandFactory.buildCommand(myQueueOfElements.peek()).execute(this)
    }
  }

  private fun createQueueOfCommands(root: Element): BlockingQueue<Element> {
    val commandsQueue = LinkedBlockingQueue<Element>()
    for (rootChild in root.children) {
      //if element is MouseBlocked (blocks all mouse events) than add all children inside it.
      if (isMouseBlock(rootChild)) {
        if (rootChild.children != null) {
          commandsQueue.add(rootChild) //add block element
          for (mouseBlockChild in rootChild.children) {
            if (isCaretBlock(mouseBlockChild)) {
              if (mouseBlockChild.children != null) {
                commandsQueue.add(mouseBlockChild) //add block element
                commandsQueue += mouseBlockChild.children
                commandsQueue += Element(Command.CommandType.CARETUNBLOCK.toString()) //add unblock element
              }
            }
            else {
              commandsQueue.add(mouseBlockChild) //add inner elements
            }
          }
          commandsQueue.add(Element(Command.CommandType.MOUSEUNBLOCK.toString())) //add unblock element
        }
      }
      else if (isCaretBlock(rootChild)) {
        if (rootChild.children != null) {
          commandsQueue.add(rootChild) //add block element
          commandsQueue += rootChild.children
          commandsQueue += Element(Command.CommandType.CARETUNBLOCK.toString()) //add unblock element
        }
      }
      else {
        commandsQueue.add(rootChild)
      }
    }
    return commandsQueue
  }

  private fun isMouseBlock(el: Element): Boolean {
    return el.name.toUpperCase() == Command.CommandType.MOUSEBLOCK.toString()
  }

  private fun isCaretBlock(el: Element): Boolean {
    return el.name.toUpperCase() == Command.CommandType.CARETBLOCK.toString()
  }

}
