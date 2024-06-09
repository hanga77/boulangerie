package lab.hang.boulangerie.services;

import lab.hang.boulangerie.dto.SessionDTO;
import lab.hang.boulangerie.entity.Session;

import java.util.List;

public interface SessionServices {
    void SaveSession(Session session);
    SessionDTO getLastSession();

    Session getLastSessions();

    List<SessionDTO> getAllSession();

}
