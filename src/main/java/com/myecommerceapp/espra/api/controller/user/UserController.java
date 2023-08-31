package com.myecommerceapp.espra.api.controller.user;

import com.myecommerceapp.espra.model.Address;
import com.myecommerceapp.espra.model.LocalUser;
import com.myecommerceapp.espra.model.dao.AddressDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private AddressDAO dao;

    @GetMapping("/{userId}/address")
    public ResponseEntity<List<Address>> getAddress(@AuthenticationPrincipal LocalUser user, @PathVariable("userId") Long userId){
        if (!userHasPermission(user, userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dao.findByUser_Id(userId));
    }

    @PutMapping("/{userId}/address")
    public ResponseEntity<Address> updateAddress(@AuthenticationPrincipal LocalUser user, @PathVariable("userId") Long userId, @RequestBody Address address){
        if (!userHasPermission(user, userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        address.setId(null);
        LocalUser refUser = new LocalUser();
        refUser.setId(userId);
        address.setUser(refUser);
        return ResponseEntity.ok(dao.save(address));
    }

    @PatchMapping("{userId}/address/{addressId}")
    public ResponseEntity<Address> updateExistingAddress(@AuthenticationPrincipal LocalUser user, @PathVariable("userId") Long userId,
                                                         @PathVariable("addressId") Long addressId, @RequestBody Address address){
        if (!userHasPermission(user, userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (address.getId() == addressId){
            Optional<Address> optOriginalAddress = dao.findById(addressId);
            if (optOriginalAddress.isPresent()){
                LocalUser originalUser = optOriginalAddress.get().getUser();
                if (originalUser.getId() == userId){
                    address.setUser(originalUser);
                    return ResponseEntity.ok(dao.save(address));
                }
            }
        }
        return ResponseEntity.badRequest().build();
    }

    private boolean userHasPermission(LocalUser user, Long userId){
        return Objects.equals(user.getId(), userId);
    }

}
