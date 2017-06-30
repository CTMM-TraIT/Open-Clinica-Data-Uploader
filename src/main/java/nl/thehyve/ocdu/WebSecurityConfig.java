/*
 * Copyright © 2016-2017 The Hyve B.V. and Netherlands Cancer Institute (NKI).
 *
 * This file is part of OCDI (OpenClinica Data Importer).
 *
 * OCDI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OCDI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OCDI. If not, see <http://www.gnu.org/licenses/>.
 */

package nl.thehyve.ocdu;

/**
 * Created by piotrzakrzewski on 21/03/16.
 */


import nl.thehyve.ocdu.security.CustomPasswordEncoder;
import nl.thehyve.ocdu.security.ExUsernamePasswordAuthenticationFilter;
import nl.thehyve.ocdu.security.OcSOAPAuthenticationProvider;
import nl.thehyve.ocdu.security.OcUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    private static final Logger log = LoggerFactory.getLogger(WebSecurityConfig.class);


    private AuthenticationFailureHandler authenticationFailureHandler = (request, response, e) -> {
        log.error("Error: " + e.getMessage());
        response.sendRedirect(request.getContextPath()+"/views/login?error");
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/", "/views/data").authenticated()
                .and()
                .formLogin()
                .permitAll()
                .loginPage("/views/login").defaultSuccessUrl("/views/data")
                .permitAll()
                .and()
                .logout()
                .permitAll();


        http.csrf().disable();
        ExUsernamePasswordAuthenticationFilter customFilter = new ExUsernamePasswordAuthenticationFilter();
        customFilter.setAuthenticationManager(authenticationManager);
        customFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
        http.addFilter(customFilter);
    }

    @Bean(name = "myAuthenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
       /* auth
                .inMemoryAuthentication()
                .withUser("user").password("password").roles("USER");*/
        auth.authenticationProvider(ocSOAPAuthenticationProvider)
                .userDetailsService(ocUserDetailsService)
                .passwordEncoder(new CustomPasswordEncoder());
    }


    @Autowired
    OcSOAPAuthenticationProvider ocSOAPAuthenticationProvider;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    OcUserDetailsService ocUserDetailsService;

}
