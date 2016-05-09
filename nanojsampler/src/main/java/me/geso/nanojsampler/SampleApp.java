package me.geso.nanojsampler;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

/**
 * Created by tokuhirom on 5/9/16.
 */
public class SampleApp {
    public static void main(String[] args) throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        Nanojsampler nanojsampler = new Nanojsampler();
        nanojsampler.initJmx();

        new Thread(() -> {
            while (true) {
                nanojsampler.poll();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).run();

    }
}
