/*
 * The MIT License
 *
 * Copyright 2016 yarhoslavme.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.yarhoslav.actors;

import me.yarhoslav.ymactors.core.DefaultActorHandler;
import me.yarhoslav.ymactors.core.mensajes.PoisonPill;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * DecrypterActor - Decrypt a 22 chart length. Encrypted Server information from
 * Slither.io
 *
 * @author yarhoslavme
 */
public class DecrypterActor extends DefaultActorHandler {

    private static final Logger LOG = LogManager.getLogger(DecrypterActor.class);

    //Messages able to react
    public enum MSGS {
        DECRYPT
    }

    //Message with response
    public final class Response {

        private final String response;
        private final int port;

        public Response(String pIP, int pPort) {
            response = pIP;
            port = pPort;
        }

        /**
         * @return the response
         */
        public String getResponse() {
            return response;
        }
        
        public int getPort() {
            return port;
        }
    }

    public enum STATUS {
        INITIATED,
        DECRYPTING,
        CLOSED
    }

    //Working variables
    private final String encrypted;
    private STATUS status;

    /**
     *
     * @param pEncrypted
     */
    public DecrypterActor(String pEncrypted) throws IllegalArgumentException {
        LOG.traceEntry("constructor");

        //Verify Argument exact 22 chars length
        if (pEncrypted.length() != 22) {
            throw LOG.throwing(Level.DEBUG, new IllegalArgumentException("Wrong length for encrypted string"));
        }

        encrypted = pEncrypted;
        status = STATUS.INITIATED;
        LOG.traceExit("constructor");
    }

    private char[] calculateBytes() {
        char[] chars = encrypted.toCharArray();
        char[] bytes = new char[chars.length];

        LOG.traceEntry("convertString");

        for (int i = 0; i < chars.length; i++) {
            int nibble = (chars[i] - 'a' - i * 7) % 26;
            if (nibble < 0) {
                nibble += 26;
            }
            bytes[i] = (char) nibble;
        }

        return LOG.traceExit(bytes);
    }

    private char[] calculateIP(char[] pBytes) {
        char[] ip = new char[11];
        
        LOG.traceEntry("calculateIP");

        int j = 0;
        for (int i = 0; i < pBytes.length; i = i + 2) {
            ip[j] = (char) (pBytes[i] * 16 + pBytes[i + 1]);
            j++;
        }

        return LOG.traceExit(ip);
    }
    
    private String convertIP(char[] pBytes) {
        StringBuilder convertedIP = new StringBuilder();
        
        LOG.traceEntry("convertIP");
        
        for (int j = 0; j < 4; j++) {
            convertedIP.append(Integer.toString(pBytes[j]));
            if (j < 3) {
                convertedIP.append(".");
            }
        }        
        
        return LOG.traceExit(convertedIP.toString());
    }
    
    private int calculatePort(char[] pBytes) {
        int port;
        
        LOG.traceEntry("calculatePort");

        port = pBytes[4] * 256 * 256 + pBytes[5] * 256 + pBytes[6];
        
        return LOG.traceExit(port);
    }

    private void decrypt() {
        LOG.traceEntry("decrypt");

        LOG.debug("Recibido: {}", encrypted);
        status = STATUS.DECRYPTING;
        char[] bytes = calculateBytes();
        char[] ip = calculateIP(bytes);
        String convertedIP = convertIP(ip);
        int port = calculatePort(ip);
        //Sending back the decrypted string 
        LOG.debug("Convertido: {}:{}", convertedIP, port);
        getMyself().getSender().tell(new Response(convertedIP, port), getMyself());
        status = STATUS.CLOSED;

        //TODO: Create EmptyActor en YMActors
        getMyself().tell(PoisonPill.getInstance(), getMyself());
        LOG.traceExit("decrypt");
    }

    //TODO: handle excpetions in YMACtor and throw Exception on @Process.  Maybe rename it to onReceive.
    @Override
    public void process(Object msg) {
        LOG.traceEntry("process");
        if (msg instanceof MSGS) {
            if (((MSGS) msg == MSGS.DECRYPT) && (status == STATUS.INITIATED)) {
                decrypt();
            }
        } else {
            //TODO: unhandled messages.  Possible update in YMActors
        }
        LOG.traceExit("process");
    }

}
