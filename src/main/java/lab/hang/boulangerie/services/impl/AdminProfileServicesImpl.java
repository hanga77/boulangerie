package lab.hang.boulangerie.services.impl;



import lab.hang.boulangerie.dto.AdminProfileDTO;
import lab.hang.boulangerie.entity.AdminProfile;
import lab.hang.boulangerie.repository.AdminProfileRepository;
import lab.hang.boulangerie.services.AdminProfileServices;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AdminProfileServicesImpl implements AdminProfileServices {
    private final AdminProfileRepository adminProfileRepository;

    private AdminProfileDTO mapToAdminProfileDTO(AdminProfile adminProfile){
        AdminProfileDTO adminProfileDTO = new AdminProfileDTO();
        adminProfileDTO.setUserAccountId(adminProfile.getUserAccountId());
        adminProfileDTO.setUserId(adminProfile.getUserId());
        adminProfileDTO.setFirstName(adminProfile.getFirstName());
        adminProfileDTO.setLastname(adminProfile.getLastname());
        adminProfileDTO.setPoste(adminProfile.getPoste());
        adminProfileDTO.setProfilePhoto(adminProfile.getProfilePhoto());

        return adminProfileDTO;
    }
}
