package mb.spoofax.runtime.pie.legacy

import mb.pie.api.*
import mb.pie.vfs.path.PPath
import org.metaborg.core.project.IProject
import org.metaborg.core.project.ISimpleProjectService

typealias TransientProject = OutTransientEquatable<IProject, PPath>

class CoreLoadProj : TaskDef<PPath, TransientProject> {
  companion object {
    const val id = "coreLoadProj"
  }

  override val id = Companion.id
  override fun ExecContext.exec(input: PPath): TransientProject {
    val spoofax = Spx.spoofax()
    val projLoc = input.fileObject
    var project = spoofax.projectService.get(projLoc)
    if(project == null) {
      project = spoofax.injector.getInstance(ISimpleProjectService::class.java).create(projLoc)
    }
    project!!
    return OutTransientEquatableImpl(project, project.path)
  }
}

val IProject.path get() = this.location().pPath

fun ExecContext.loadProj(input: PPath) = requireOutput(CoreLoadProj::class.java, CoreLoadProj.id, input).v
