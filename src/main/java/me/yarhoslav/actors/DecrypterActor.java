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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author yarhoslavme
 */
public class DecrypterActor extends DefaultActorHandler {

    private static final Logger LOG = LogManager.getLogger(DecrypterActor.class);

    public enum MSGS {
        DECRYPT
    }

    public enum STATUS {
        INITIATED,
        DECRYPTING,
        CLOSED
    }

    //Working variables
    private final String encrypted;
    private String decrypted;
    private STATUS status;

    public DecrypterActor(String pEncrypted) {
        //TODO: Check illegal argument - string different to 22 chars - throw Illegalargument exception.
        LOG.traceEntry("constructor");
        encrypted = pEncrypted;
        status = STATUS.INITIATED;
        LOG.traceExit("constructor");
    }

    public void decrypting() {
        LOG.traceEntry("decrypting");
        LOG.debug("Recibido: {}", encrypted);
        status = STATUS.DECRYPTING;

        //TODO: Decrypter algorithm
        StringBuilder txtIP = new StringBuilder();
        char[] chars = encrypted.toCharArray();
        char[] bytes = new char[chars.length];
        for (int i = 0; i < chars.length; i++) {
            int nibble = (chars[i] - 'a' - i * 7) % 26;
            if (nibble < 0) {
                nibble += 26;
            }
            bytes[i] = (char) nibble;
        }

        //For test purpose
        StringBuilder txtBytes = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            txtBytes.append(Integer.toString(bytes[i]));
            txtBytes.append(",");
        }
        LOG.debug(txtBytes.toString());

        char[] ip = new char[11];
        int j = 0;
        for (int i = 0; i < bytes.length; i = i + 2) {
            ip[j] = (char) (bytes[i] * 16 + bytes[i + 1]);
            j++;
        }

        StringBuilder txtANT = new StringBuilder();
        for (int i = 0; i < ip.length; i++) {
            txtANT.append(Integer.toString(ip[i]));
            txtANT.append(",");
        }
        LOG.debug(txtANT.toString());

        for (j = 0; j < 4; j++) {
            txtIP.append(Integer.toString(ip[j]));
            if (j < 3) {
                txtIP.append(".");
            }
        }

        //Calcule Port
        txtIP.append(":");
        int port = ip[4] * 256 * 256 + ip[5] * 256 + ip[6];
        txtIP.append(Integer.toString(port));
        decrypted = txtIP.toString();

        //Sending back the result decrypted
        LOG.debug("Convertido: {}", decrypted);
        getMyself().getSender().tell(decrypted, getMyself());
        status = STATUS.CLOSED;
        LOG.traceExit("decrypting");
    }

    //TODO: handle excpetions in YMACtor and throw Exception on @Process.  Maybe rename it to onReceive.
    @Override
    public void process(Object msg) {
        LOG.traceEntry("process");
        if (msg instanceof MSGS) {
            if ((MSGS) msg == MSGS.DECRYPT) {
                if (status == STATUS.INITIATED) {
                    decrypting();
                }
            }
        } else {
            //TODO: unhandled messages.  Possible update in YMActors
        }
        LOG.traceExit("process");
    }

}
