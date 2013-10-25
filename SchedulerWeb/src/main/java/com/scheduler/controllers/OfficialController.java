package com.scheduler.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.scheduler.models.Appointment;
import com.scheduler.services.AppointmentService;
import com.scheduler.services.NotificationService;

@RequestMapping("/official")
@Controller

public class OfficialController {

	@Autowired(required=true)
	private AppointmentService appointmentService;

	@Autowired(required = true)
	private NotificationService notificationService; 


	
	@RequestMapping(value="/meeting/finish",method=RequestMethod.POST)
	public String finishMeeting(@ModelAttribute("appointment") Appointment appointment,Model model)
	//public String finishMeeting(HttpServletRequest req,Model model)
	{
		
		//Appointment appointment=new Appointment();
		//appointment.setAppointmentId(Integer.parseInt(req.getParameter("appointment_id")));
		//appointment.setMeetingNotes(req.getParameter("meeting_notes"));
		boolean meetingFinished=appointmentService.finishAppointment(appointment);
		if(meetingFinished)
		{
			model.addAttribute("finish", true);
		}
		else
		{
			model.addAttribute("finish",false);
		}
		return "redirect:/official/meeting/viewqueue";
	}
	
	@RequestMapping(value="/meeting/testmeeting",method=RequestMethod.GET)
	public String test(Model model)
	{
		return "meeting/meeting";
	}

	@RequestMapping(value = "/meeting/start/{app_id}", method = RequestMethod.GET)
	public String startMeeting(Model model, @PathVariable("app_id") int app_id) {
		
		// get official id from the session variable
		int official_id = 1234; // hardcoded value
		
		// Code to start appointment
		Appointment startedAppointment = appointmentService.startAppointmentById(app_id, official_id);
		
		if(!startedAppointment.equals(null)) {
			
			// if appointment is marked as started
			model.addAttribute("started", "true");
			model.addAttribute("appointment", startedAppointment);
			
		}
		else {
			
			// if appointment couldn't be marked as started
			model.addAttribute("started", "false");
		}
		
		return "meeting/meeting";
		
	}

}