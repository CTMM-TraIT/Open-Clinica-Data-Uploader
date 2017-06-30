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

package nl.thehyve.ocdu.security;

import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.OcUserDetails;
import nl.thehyve.ocdu.services.OpenClinicaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * User authentication is done against OpenClinica WebServices.
 * Created by piotrzakrzewski on 18/04/16.
 */
@Component
public class OcSOAPAuthenticationProvider implements AuthenticationProvider {


    @Autowired
    OpenClinicaService openClinicaService;

    @Autowired
    OcUserDetailsService userDetailsService;

    private static final Logger log = LoggerFactory.getLogger(OcSOAPAuthenticationProvider.class);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        OcUserDetails userDetails = (OcUserDetails) userDetailsService.loadUserByUsername(name); //TODO: should not call userDetails here, find a better way

        String ocEnvironment =  userDetails.getOcEnvironment();//"http://ocdu-openclinica-dev.thehyve.net/OpenClinica-ws"; //  usr.getOcEnvironment();
        CustomPasswordEncoder encoder = new CustomPasswordEncoder();
        password = encoder.encode(password);

        try {
            if (openClinicaService.isAuthenticated(name, password, ocEnvironment)) {
                List<GrantedAuthority> grantedAuths = new ArrayList<>();
                grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
                return new UsernamePasswordAuthenticationToken(name, password, grantedAuths);
            } else {
                log.error("Failed to authenticate user: "+ name +" against Open Clinica instance: "+ ocEnvironment);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        boolean supports = authentication.equals(UsernamePasswordAuthenticationToken.class);
        log.debug("Supports method fired, result: "+ supports);
        return supports;
    }
}
