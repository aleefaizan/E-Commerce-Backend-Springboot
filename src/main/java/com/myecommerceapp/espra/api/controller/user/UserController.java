package com.myecommerceapp.espra.api.controller.user;

import com.myecommerceapp.espra.api.model.DataChange;
import com.myecommerceapp.espra.model.Address;
import com.myecommerceapp.espra.model.LocalUser;
import com.myecommerceapp.espra.model.dao.AddressDAO;
import com.myecommerceapp.espra.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private UserServiceImpl userService;


    @GetMapping("/{userId}/address")
    public ResponseEntity<List<Address>> getAddress(@AuthenticationPrincipal LocalUser user, @PathVariable("userId") Long userId){
        if (!userService.userHasPermissionToUser(user, userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dao.findByUser_Id(userId));
    }

    @PutMapping("/{userId}/address")
    public ResponseEntity<Address> updateAddress(@AuthenticationPrincipal LocalUser user, @PathVariable("userId") Long userId, @RequestBody Address address){
        if (!userService.userHasPermissionToUser(user, userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        address.setId(null);
        LocalUser refUser = new LocalUser();
        refUser.setId(userId);
        address.setUser(refUser);
        Address savedAddress = dao.save(address);
        simpMessagingTemplate.convertAndSend("/topic/user/" + userId + "/address/",
                new DataChange<>(DataChange.ChangeType.INSERT, address));
        return ResponseEntity.ok(savedAddress);
    }

    @PatchMapping("{userId}/address/{addressId}")
    public ResponseEntity<Address> updateExistingAddress(@AuthenticationPrincipal LocalUser user, @PathVariable("userId") Long userId,
                                                         @PathVariable("addressId") Long addressId, @RequestBody Address address){
        if (!userService.userHasPermissionToUser(user, userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (address.getId() == addressId){
            Optional<Address> optOriginalAddress = dao.findById(addressId);
            if (optOriginalAddress.isPresent()){
                LocalUser originalUser = optOriginalAddress.get().getUser();
                if (originalUser.getId() == userId){
                    address.setUser(originalUser);
                    Address savedAddress = dao.save(address);
                    simpMessagingTemplate.convertAndSend("/topic/user/" + userId + "/address/",
                            new DataChange<>(DataChange.ChangeType.UPDATE, address));
                    return ResponseEntity.ok(savedAddress);
                }
            }
        }
        return ResponseEntity.badRequest().build();
    }
}
