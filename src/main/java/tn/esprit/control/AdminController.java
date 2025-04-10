package tn.esprit.control;

import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController

@PreAuthorize("hasRole('ADMIN')")
public class AdminController {


@GetMapping
@PreAuthorize("hasAnyAuthority('admin:read')")
public String get(){
    return "get :: admin controller";
}

    @PostMapping
    @PreAuthorize("hasAnyAuthority('admin:create')")
    public String post(){
        return "post :: admin controller";
    }
    @PutMapping
    @PreAuthorize("hasAnyAuthority('admin:update')")
    public String put(){
        return "put :: admin controller";

    }@DeleteMapping
    @PreAuthorize("hasAnyAuthority('admin:delete')")

    public String delete(){
        return "delete :: admin controller";
    }

}
