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

import nl.thehyve.ocdu.OCEnvironmentsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Filter required to pass on user selected OcEnvironment during login.
 *
 * Created by piotrzakrzewski on 18/04/16.
 */
public class ExUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final Logger log = LoggerFactory.getLogger(ExUsernamePasswordAuthenticationFilter.class);

    private OCEnvironmentsConfig ocEnvironmentsConfig;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        final String ocEnvironmentName = request.getParameter(OCEnvironmentsConfig.OC_ENV_ATTRIBUTE_NAME);
        Optional<OCEnvironmentsConfig.OCEnvironment> ocEnvironment =
                ocEnvironmentsConfig.getOcEnvironments()
                        .stream()
                        .filter( ocEnvironment1 -> ocEnvironment1.getName().equals(ocEnvironmentName))
                        .findFirst();
        String targetURL = ocEnvironment.get().getUrl();
        log.info("Attempted authentication against: " + ocEnvironment.get().getName());
        String password = request.getParameter("password");
        CustomPasswordEncoder encoder = new CustomPasswordEncoder();
        password = encoder.encode(password);
        request.getSession().setAttribute("ocwsHash", password);
        request.getSession().setAttribute(OCEnvironmentsConfig.OC_ENV_ATTRIBUTE_NAME, targetURL);

        return super.attemptAuthentication(request, response);
    }

    public ExUsernamePasswordAuthenticationFilter(OCEnvironmentsConfig ocEnvironmentsConfig) {
        super.setPostOnly(true);  //TODO: should be defined as a Bean
        this.ocEnvironmentsConfig = ocEnvironmentsConfig;
    }
}
