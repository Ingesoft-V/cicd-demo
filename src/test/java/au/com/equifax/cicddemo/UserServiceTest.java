package au.com.equifax.cicddemo;

import au.com.equifax.cicddemo.domain.User;
import au.com.equifax.cicddemo.service.UserService;
import au.com.equifax.cicddemo.service.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceTest {

    private UserService service;

    @BeforeEach
    public void setUp() {
        service=new UserServiceImpl();
    }

    @Test
    public void addUSerTest(){
        User usr =User.UserBuilder.anUser()
                .withEmail("helderklemp@gmail.com")
                .withLogin("hklemp")
                .withName("Helder Klemp").build();
        service.save(usr);
        Assertions.assertEquals(1,service.getUsersTotal());
    }

    @Test
    public void validateUserTest(){
        //BAsic User
        User usr =User.UserBuilder.anUser()
                .withEmail("helderklemp@gmail.com").build();
        assertThrows(IllegalArgumentException.class, () -> {
            service.save(usr);
        });
    }

    @Test
    public void validateUserCollectionTest(){
        User usr =User.UserBuilder.anUser()
                .withEmail("another@gmail.com")
                .withLogin("another")
                .withName("another").build();
        service.save(usr);

        service.getUsers().stream().forEach(user ->
                Assertions.assertEquals(user,usr));
    }

    @Test
    public void validateFindByIdTest(){
        User usr =User.UserBuilder.anUser()
                .withEmail("another@gmail.com")
                .withLogin("another")
                .withId(10)
                .withName("another").build();
        service.save(usr);

        Assertions.assertEquals(usr,service.findById(10).get());
    }
}
