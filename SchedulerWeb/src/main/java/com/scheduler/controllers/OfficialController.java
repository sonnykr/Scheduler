package com.scheduler.controllers;

import java.util.List;

import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.scheduler.models.Announcement;
import com.scheduler.models.Appointment;
import com.scheduler.models.AppointmentList;
import com.scheduler.models.Client;
import com.scheduler.models.GeneralUser;
import com.scheduler.models.Notification;
import com.scheduler.models.OfficialUser;
import com.scheduler.request.MailMail;
import com.scheduler.services.AnnouncementService;
import com.scheduler.services.AppointmentService;
import com.scheduler.services.GeneralUserService;
import com.scheduler.services.NotificationService;
import com.scheduler.services.OfficialUserService;
import com.scheduler.services.UserService;

@RequestMapping("/official")
@Controller
@Slf4j
public class OfficialController {

	@Autowired(required = true)
	private AppointmentService appointmentService;

	@Autowired(required = true)
	private NotificationService notificationService;

	@Autowired(required = true)
	private AnnouncementService announcementService;
	
	public List<AppointmentList> listofAppointment;
	
	@Autowired(required = true)
	private OfficialUserService officialuserService;

	@RequestMapping(value = "/meeting/finish", method = RequestMethod.POST)
	public String finishMeeting(
			@ModelAttribute("appointment") Appointment appointment, Model model) {
		// Code to Finish Appointment
		boolean meetingFinished = appointmentService
				.finishAppointment(appointment);
		if (meetingFinished) {
			model.addAttribute("finish", true);
		} else {
			model.addAttribute("finish", false);
		}
		// Redirecting to view the queue
		return "redirect:/official/meeting/viewqueue";
	}

	@RequestMapping(value = "/meeting/viewqueue", method = RequestMethod.GET)
	public String viewQueue(Model model) {
		System.out.println("view queue started");

		int departmentId=1;
		String appointmentDate="2013-11-13";
		listofAppointment= appointmentService.getAllAppointment(departmentId,appointmentDate);
		model.addAttribute("appointmentList",listofAppointment);

		// passing blank announcement object
		model.addAttribute("announcement", new Announcement());
		// Redirecting to view the queue
		return "meeting/viewqueue";
	}

	@RequestMapping(value = "/meeting/start", method = RequestMethod.POST)
	public String startMeeting(Model model) {
		//insert the following fields to official user session
		// get official_id and dept_id from the session variable
		int official_id = 3; // hardcoded value
		int department_id = 1; // hardcoded value

		Appointment nextAppointment = appointmentService
				.findNextAppointment(department_id);

		// Code to start appointment
		Appointment startedAppointment = appointmentService
				.startAppointmentById(nextAppointment.getAppointmentId(),
						official_id);

		if (!startedAppointment.equals(null)) {

			// Get next user in Queue (and send a push notification)
			GeneralUser nextUser = appointmentService
					.getNextUserInQueue(department_id);
			//
			Notification notification = new Notification();
			notification.setOfficialId(official_id);
			notification.setUserId(nextUser.getUserId());
			notification.setNotificationHeader("Meeting starting soon!");
			notification
					.setNotificationDescription("You are the next person in queue");
			boolean notifyNextUser = notificationService.notifyUser(
					nextUser.getGcmRegId(), notification);
			if (notifyNextUser) {
				model.addAttribute("nextUserNotified", true);
			}

			// if appointment is marked as started
			model.addAttribute("started", "true");
			model.addAttribute("appointment", startedAppointment);

		} else {

			// if appointment couldn't be marked as started
			model.addAttribute("started", "false");
		}

		return "meeting/meeting";

	}

	@RequestMapping(value = "/meeting/broadcast", method = RequestMethod.POST)
	public String broadcastMessage(
			@ModelAttribute("announcement") Announcement announcement,
			Model model) {
		log.info("Got " + announcement.getAnnouncementHeader());

		// TODO insert the following fields to official user session
		// get official_id and dept_id from the session variable
		int official_id = 1234; // hardcoded value

		announcement.setOfficialId(official_id);
		int announcement_id = announcementService
				.addNewAnnouncement(announcement);

		boolean broadcasted = announcementService.addUserAnnouncement(
				listofAppointment, announcement_id,
				announcement.getAnnouncementHeader());

		// Redirecting to view the queue
		return "redirect:/official/meeting/viewqueue";
	}
	@RequestMapping(value = "meeting/late", method = RequestMethod.GET)
	public String userLate(RedirectAttributes ra, Model model) {

		int result;
		int appointmentId=1;
		try {
			result = appointmentService.userLate(appointmentId);
			model.addAttribute("result", result);
		} catch (BadSqlGrammarException e) {
			model.addAttribute("error", e.getMessage());
			System.out.println(e.getMessage());
		}

		return "redirect:/official/meeting/viewqueue";
	}
	
	
	@RequestMapping(value = "meeting/testmeeting", method = RequestMethod.GET)
	public String testme(Model model) {
		
		ApplicationContext context = new ClassPathXmlApplicationContext("Spring-Mail.xml");
		MailMail mm = (MailMail) context.getBean("mailMail");
        mm.sendMail("Scheduler App", "This is a Test Email \n your activation code : 85647555 ","sanket.scorp@gmail.com");
		
		return "meeting/testmeeting";
	}
	// Author - Devraj Valecha
			// Usage - Login for official user
			// client
		@RequestMapping(value = "/login", method = RequestMethod.GET)
		public String loginOfficial(Model model)
		{
			return "officialuser/loginofficial";
		}
		
		@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
		public String authenticateOfficial(@RequestParam("userName") String userName,
				@RequestParam("password") String password, Model model,
				HttpSession session) {
			OfficialUser o1 = new OfficialUser();
			o1.setOfficialName(userName);
			o1.setPassword(password);
			OfficialUser result = officialuserService.authenticate(o1);
			if (result.getOfficialId()>0) {
				String name = result.getFirstName();
				int id = result.getOfficialId();
				session.setAttribute("officialUserName", userName);
				session.setAttribute("officialName", name);
				session.setAttribute("officialId", id);
			} else {
				model.addAttribute("result", "Login Failed");
				return "officialuser/errorofficiallogin";
			}
			return "redirect:dashboard";
		}
		

		@RequestMapping(value = "/dashboard", method = RequestMethod.GET)
		public String showDashboard(Model model)
		{
			return "officialuser/dashboard";
		}
		

}
