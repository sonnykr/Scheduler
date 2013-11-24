package com.scheduler.controllers;
 
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
 
@Controller
public class SecurityController {
 
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String defaultPage(ModelMap map) {
    	return "home/index";
    }
    
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String loginRedirect(ModelMap map) {
    	
    	User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	System.out.println(user.getAuthorities().toString());
 
    	if(user.getAuthorities().contains(new SimpleGrantedAuthority("CL"))) {
    		return "redirect:client/dashboard";
    	} else if(user.getAuthorities().contains(new SimpleGrantedAuthority("OU"))) {
    		return "redirect:official/dashboard";
    	} else if(user.getAuthorities().contains(new SimpleGrantedAuthority("GU"))) {
    		return "redirect:generaluser/dashboard";
    	}
        return "home/home";
    }
 
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(ModelMap model) {
 
    	if(!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
    		return "redirect:list";
    	}
        return "home/login";
    }
 
    @RequestMapping(value = "/accessdenied", method = RequestMethod.GET)
    public String loginerror(ModelMap model) {
        model.addAttribute("error", "true");
        return "home/denied";
    }
 
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(ModelMap model) {
        return "home/logout";
    }
}