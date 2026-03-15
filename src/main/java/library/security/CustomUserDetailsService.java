package library.security;

import library.model.User;
import library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(login);
        if (user == null) {
            user = userRepository.findByName(login);
        }

        if (user == null) {
            throw new UsernameNotFoundException("User not found with login: " + login);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) // Внутри Spring Security храним email как основной идентификатор
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
