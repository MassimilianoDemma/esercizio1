package co.develhope.bugtracker.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import co.develhope.bugtracker.controller.dto.*;
import co.develhope.bugtracker.entity.Ordine;
import co.develhope.bugtracker.exception.ConflictException;
import co.develhope.bugtracker.exception.ForbiddenException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import co.develhope.bugtracker.entity.Utente;
import co.develhope.bugtracker.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	private EntityManager entityManager;
	@Autowired
	private UserRepository userRepository;
	@Value("${userphisicaldelete}")//CREATA PROPRIETIES PER SCEGLIERE CANCELLAZIONE LOGICA O FISICA
	private Boolean userPhisicaldelete;
	public CreateUserResponseDto createUser(CreateUserRequestDto request) {

		Optional<Utente> oUser = userRepository.findByUsername(request.getUsername());
		if(oUser.isPresent()){
		oUser.orElseThrow(() -> new ConflictException());}
		Utente user = new Utente();
		user.setUsername("Aldo Baglio");
		user.setPassword("Password");
		user.setDeleted(false);
		user=userRepository.save(user);// Ora l'ID non è null

		CreateUserResponseDto createUserResponseDto = new CreateUserResponseDto();
		createUserResponseDto.setId(user.getId());
		return createUserResponseDto ;
	}

	
	private Utente fromRequestToEntity(CreateUserRequestDto request) {
		Utente utente = new Utente();
		utente.setPassword(request.getPassword());
		utente.setUsername(request.getUsername());
		return utente;
	}
	
	private CreateUserResponseDto fromEntityToResponse(Utente utente) {
		CreateUserResponseDto createUserResponseDto = new CreateUserResponseDto();
		createUserResponseDto.setId(utente.getId());
		return createUserResponseDto;
	}

    public BaseResponse deleteUser(DeleteUserRequestDto delete) {

		Optional<Utente> user = userRepository.findById(delete.getId());
		if(user.isEmpty()) {
			throw new RuntimeException(); //NIENTE ELSE PERCHé TANTO ESCE CON L'ECCEZIONE
		}
			Utente useru = user.get();

		if(userPhisicaldelete){
			userRepository.delete(useru);
		}
		else{
			useru.setDeleted(true);
			userRepository.save(useru);
		}
		return new BaseResponse();
    }

	public BaseResponse changePassword(ChangePasswordRequestDto request) {
		Optional<Utente> outente = userRepository.findByUsername(request.getUsername());

		if(outente.isEmpty()){
			throw new RuntimeException("user with username " + request.getUsername() + " not found");
		}

		Utente utente = outente.get();

		if(utente.getPassword().equals(request.getOldPassword())){
			utente.setPassword(request.getNewPassword());

			userRepository.save(utente);
			return new BaseResponse();
		} else {
			throw new ForbiddenException("wrong password");
		}

	}

	public List<UserAspirapolvereResponseDto> getAspirapolvere() {
	//Tutti gli utenti che hanno fatto un ordine->Poi si seleziona gli ordine con aspirapolvere
	 //Query query = entityManager.createNativeQuery("SELECT U.* FROM UTENTE U INNER JOIN ORDINE O ON  U.ID = O.UTENTE_ID WHERE O.ITEM = 'ASPIRAPOLVERE' ", Utente.class); SPOSTATO IN REPOSITORY
	 //L'oggetto query permette di eseguire la query
	 List<Utente> utenti = userRepository.getAspirapolvere();
	 List<Utente> utenti2 = new ArrayList<>();//Discorso sulle enclosure e sul fatto che le lambda utilizzano solo final

			utenti.stream().forEach(item->{
				if (!utenti2.contains(item)) {
					utenti2.add(item);
				}
			});
		List<UserAspirapolvereResponseDto> responseDtoList = new ArrayList<>();
		for (Utente utente : utenti2){

			List<Integer> idOrdineAspirapolvere = utente.getOrdini().stream().filter(m->m.getItem().equalsIgnoreCase("aspirapolvere")).map(Ordine::getId).toList();
			UserAspirapolvereResponseDto responseDto = new UserAspirapolvereResponseDto();
			responseDto.setUsername(utente.getUsername());
			responseDto.setListaID(idOrdineAspirapolvere);
			responseDtoList.add(responseDto);
		}

		return responseDtoList;
	}

}