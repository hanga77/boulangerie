package lab.hang.boulangerie.services.impl;

import lab.hang.boulangerie.dto.SessionDTO;
import lab.hang.boulangerie.entity.Session;
import lab.hang.boulangerie.repository.SessionRepository;
import lab.hang.boulangerie.services.SessionServices;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SessionServicesImpl implements SessionServices {
    private final SessionRepository sessionRepository;
    @Override
    public void SaveSession(Session session) {

    }

    @Override
    public SessionDTO getLastSession() {
        Pageable pageable = PageRequest.of(0,1, Sort.Direction.DESC,"sessionID");
        Page<Session> lastPage = sessionRepository.findAll(pageable);

        return lastPage.stream().findFirst().map(this::mapToSessionDTO).get();
    }

    @Override
    public Session getLastSessions() {
        Pageable pageable = PageRequest.of(0,1, Sort.Direction.DESC,"sessionID");
        Page<Session> lastPage = sessionRepository.findAll(pageable);

        return lastPage.stream().findFirst().get();
    }
    @Override
    public List<SessionDTO> getAllSession() {
        return sessionRepository.findAll().stream().map(this::mapToSessionDTO).collect(Collectors.toList());
    }

    public Session mapToSession(SessionDTO sessionDTO){
        Session session = new Session();
        session.setSessionID(sessionDTO.getSessionID());
        session.setHashCode(sessionDTO.getHashCode());
        session.setDateCreated(sessionDTO.getDateCreated());
        session.setUsersId(sessionDTO.getUsersId());
        return session;
    }
    public SessionDTO mapToSessionDTO(Session sessionDTO){
        SessionDTO session = new SessionDTO();
        session.setSessionID(sessionDTO.getSessionID());
        session.setHashCode(sessionDTO.getHashCode());
        session.setDateCreated(sessionDTO.getDateCreated());
        session.setUsersId(sessionDTO.getUsersId());
        return session;
    }


}
