package com.dw.backend.doablewellbeingbackend.business.user;

import com.dw.backend.doablewellbeingbackend.common.exception.NotFoundException;
import com.dw.backend.doablewellbeingbackend.presistence.entity.RoleEntity;
import com.dw.backend.doablewellbeingbackend.presistence.entity.UserEntity;
import com.dw.backend.doablewellbeingbackend.presistence.impl.RoleRepository;
import com.dw.backend.doablewellbeingbackend.presistence.impl.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    RoleRepository roleRepository;
    @InjectMocks
    UserServiceImpl service;


    @Test
    void existsByEmail_delegatesToRepo(){
        String email = "Example@example.com";
        when(userRepository.existsByEmailIgnoreCase(email)).thenReturn(true);

        boolean result = service.existsByEmail(email);
        assertThat(result).isTrue();
        verify(userRepository).existsByEmailIgnoreCase(email);
        verifyNoMoreInteractions(userRepository, roleRepository);
    }

    @Test
    void findByEmail_found_mapsToAppUser_andCloneSalt(){
        String email = "Example@example.com";
        byte[] salt = new  byte[] {1, 2, 3};
        var roleUser = new RoleEntity();
        roleUser.setId(1);
        roleUser.setName("user");
        var roleCoach =  new RoleEntity();
        roleCoach.setId(2);
        roleCoach.setName("coach");

        var e = new UserEntity();
        e.setId(UUID.randomUUID());
        e.setEmail(email);
        e.setFirstName("Jane");
        e.setLastName("Doe");
        e.setPasswordHash("hash");
        e.setPasswordSalt(salt);
        e.setActive(true);
        e.setDeleted(false);
        e.setRoles(new HashSet<>(Arrays.asList(roleUser, roleCoach)));

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(e));

        var opt =  service.findByEmail(email);

        assertThat(opt).isPresent();
        var appUser = opt.get();


        assertThat(appUser.getId()).isEqualTo(e.getId());
        assertThat(appUser.getEmail()).isEqualTo(email);
        assertThat(appUser.getFirstName()).isEqualTo("Jane");
        assertThat(appUser.getLastName()).isEqualTo("Doe");
        assertThat(appUser.getPasswordHash()).isEqualTo("hash");
        assertThat(appUser.isActive()).isTrue();
        assertThat(appUser.isDeleted()).isFalse();
        assertThat(appUser.getRoleNames()).containsExactlyInAnyOrder("user", "coach");

        assertThat(appUser.getPasswordSalt())
                .isNotSameAs(salt)
                .containsExactly(1, 2, 3);

        appUser.getPasswordSalt()[0] = 99;
        assertThat(e.getPasswordSalt()).containsExactly(1,2,3);

        verify(userRepository).findByEmailIgnoreCase(email);
        verifyNoMoreInteractions(userRepository, roleRepository);
    }

    @Test

    void findByEmail_notFound_empty() {
        when(userRepository.findByEmailIgnoreCase("nope@example.com")).thenReturn(Optional.empty());

        var result = service.findByEmail("nope@example.com");

        assertThat(result).isEmpty();
        verify(userRepository).findByEmailIgnoreCase("nope@example.com");
        verifyNoMoreInteractions(userRepository, roleRepository);
    }

    @Test
    void getRequired_found_returnsMapped() {
        UUID id = UUID.randomUUID();
        var e = new UserEntity();
        e.setId(id);
        e.setEmail("carol@example.com");
        e.setFirstName("Carol");
        e.setLastName("Danvers");
        e.setPasswordHash("hash");
        e.setPasswordSalt(new byte[] {7,8});
        e.setActive(true);
        e.setDeleted(false);
        e.setRoles(Collections.emptySet());

        when(userRepository.findById(id)).thenReturn(Optional.of(e));

        var appUser = service.getRequired(id);

        assertThat(appUser.getId()).isEqualTo(id);
        assertThat(appUser.getEmail()).isEqualTo("carol@example.com");
        assertThat(appUser.getRoleNames()).isEmpty();
        assertThat(appUser.getPasswordSalt()).containsExactly(7,8);
        assertThat(appUser.getPasswordSalt()).isNotSameAs(e.getPasswordSalt());

        verify(userRepository).findById(id);
        verifyNoMoreInteractions(userRepository, roleRepository);
    }

    @Test
    void getRequired_missing_throws() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRequired(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(id);
        verifyNoMoreInteractions(userRepository, roleRepository);
    }

    @Test
    void createUser_happyPath(){
        String email = "Example@example.com";
        String hash = "hash";
        byte[] salt = new  byte[] {1, 2, 3};
        String firstName = "Jane";
        String lastName = "Doe";

        RoleEntity role = new RoleEntity();
        role.setId(1);
        role.setName("user");

        when(roleRepository.findByName("user")).thenReturn(Optional.of(role));

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);


        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv ->{
            UserEntity user = inv.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        var appUser = service.createUser(email, hash, salt, firstName, lastName);

        verify(roleRepository).findByName("user");
        verify(userRepository).save(captor.capture());
        verifyNoMoreInteractions(userRepository, roleRepository);

        UserEntity saved =  captor.getValue();

        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getPasswordHash()).isEqualTo(hash);

        assertThat(saved.getPasswordSalt())
                .isNotSameAs(salt)
                .containsExactly(1, 2, 3);

        salt[0] = 99;

        assertThat(saved.getPasswordSalt()).containsExactly(1,2,3);

        assertThat(saved.getFirstName()).isEqualTo("Jane");
        assertThat(saved.getLastName()).isEqualTo("Doe");

        assertThat(saved.getRoles())
                .extracting(RoleEntity::getName)
                .containsExactly("user");

        assertThat(appUser.getEmail()).isEqualTo(email);
        assertThat(appUser.getFirstName()).isEqualTo("Jane");
        assertThat(appUser.getLastName()).isEqualTo("Doe");
        assertThat(appUser.getPasswordHash()).isEqualTo(hash);
        assertThat(appUser.getRoleNames()).containsExactly("user");
        assertThat(appUser.getPasswordSalt())
                .isNotSameAs(saved.getPasswordSalt())
                .containsExactly(1,2,3);


    }
}