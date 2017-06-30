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

import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by piotrzakrzewski on 26/04/16.
 */
public class CustomPasswordEncoder implements PasswordEncoder {

    public String encode(CharSequence rawPassword) {
         return getSha1Hexdigest(rawPassword);
    }


    private String getSha1Hexdigest(CharSequence rawPassword) {
        MessageDigest cript = null;
        try {
            cript = MessageDigest.getInstance("SHA-1");
            cript.reset();
            cript.update(rawPassword.toString().getBytes("utf8"));
            String hexBinaryRep = DatatypeConverter.printHexBinary(cript.digest());
            return hexBinaryRep.toLowerCase() ; // OpenClinica expects lowercase representation of the hex digest
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String hexDigest = getSha1Hexdigest(rawPassword);
        return hexDigest.equals(encodedPassword);
    }
}
