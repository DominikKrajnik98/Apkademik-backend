package pl.edu.pg.apkademikbackend.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.pg.apkademikbackend.WebSecurity.exceptions.InvalidPasswordException;
import pl.edu.pg.apkademikbackend.user.exception.UserNotFoundException;
import pl.edu.pg.apkademikbackend.user.model.UserDao;
import pl.edu.pg.apkademikbackend.user.model.UserDto;
import pl.edu.pg.apkademikbackend.user.repositry.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userDao;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UserNotFoundException {
        UserDao user = userDao.findByEmail(email);
        if(user == null){
            throw new UserNotFoundException(email);
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                getAuthority(user));
    }

    private Set<GrantedAuthority> getAuthority(UserDao user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        });
        return authorities;
    }

    public UserDao save(UserDto user){
        UserDao newUser = new UserDao();
        return saveUserDao(newUser,user);
    }

    public UserDao save(UserDto user, String email){
        UserDao updatedUser = userDao.findByEmail(email);
        if(updatedUser == null){
            throw new UserNotFoundException(email);
        }
        return saveUserDao(updatedUser,user);
    }
    public UserDao save(UserDto user, String email, String oldPassword){
        UserDao updatedUser = userDao.findByEmail(email);
        if(updatedUser == null){
            throw new UserNotFoundException(email);
        }
        if(!bcryptEncoder.matches(oldPassword,updatedUser.getPassword()))
            throw new InvalidPasswordException();
        return saveUserDao(updatedUser,user);
    }
    private UserDao saveUserDao(UserDao updatedUser,UserDto user){
        if(user.getPassword()!= null)
            updatedUser.setPassword(bcryptEncoder.encode(user.getPassword()));
        if(user.getName()!= null)
            updatedUser.setName(user.getName());
        if(user.getSurname()!= null)
            updatedUser.setSurname(user.getSurname());
        if(user.getEmail()!=null)
            updatedUser.setEmail(user.getEmail());
        return userDao.save(updatedUser);
    }
}