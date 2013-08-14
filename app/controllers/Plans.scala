package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._

object Plans extends Controller {
  
  def list = Action { implicit request =>
    val plans = Plan.findAllUnmatched()
    if (plans.isEmpty)
      Ok(views.html.plans.noplans())
    else
      Ok(views.html.plans.list(plans))
  }

  val form = Form(
    tuple(
      "summary" -> text,
      "details" -> optional(text)
    )
  )

  def showForm = Action { implicit request =>
    Ok(views.html.plans.form(form))
  }

  def loggedIn[A](block: User => PlainResult)(implicit request: Request[A]): PlainResult = {
    session.get("userId").flatMap { case uId => User.find(uId.toLong) }.fold {
      Redirect(routes.Users.login).flashing("error" -> "Oops, you're not logged in!")
    } { user =>
      block(user)
    }
  }

  def create = Action { implicit request =>
    loggedIn { user =>
      form.bindFromRequest.fold ({ formWithErrors =>
        Ok(views.html.plans.form(form))
      }, { case (summary, details) =>
        // Persist to DB
        val plan = Plan.create(summary, details, user)
        Redirect(routes.Plans.list()).flashing("info" -> "Successfully created a plan!")
      })
    }
  }


}

