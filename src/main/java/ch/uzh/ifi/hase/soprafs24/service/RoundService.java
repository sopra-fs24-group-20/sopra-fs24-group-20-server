package ch.uzh.ifi.hase.soprafs24.service;
import java.util.Random;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
@Service
@Transactional
public class RoundService {
    public char Generate_letter() {
    Random random = new Random();
    char randomLetter = (char) ('A' + random.nextInt(26));
    return randomLetter;
    }

}
