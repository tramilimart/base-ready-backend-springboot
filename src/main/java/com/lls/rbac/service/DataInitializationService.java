package com.lls.rbac.service;

import com.lls.rbac.entity.Permission;
import com.lls.rbac.entity.Role;
import com.lls.rbac.entity.User;
import com.lls.rbac.repository.PermissionRepository;
import com.lls.rbac.repository.RoleRepository;
import com.lls.rbac.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//@Service
public class DataInitializationService implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializationService.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializationService(RoleRepository roleRepository, 
                                   PermissionRepository permissionRepository,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            log.info("Initializing default roles and permissions...");
            initializePermissions();
            initializeRoles();
            initializeDefaultUsers();
            log.info("Data initialization completed!");
        } catch (Exception e) {
            log.error("Error during data initialization: {}", e.getMessage(), e);
            // Don't re-throw the exception to allow the application to start
        }
    }

    private void initializePermissions() {
        // User permissions
        createPermissionIfNotExists("USER_READ", "Read user data", "USER", "READ");
        createPermissionIfNotExists("USER_WRITE", "Write user data", "USER", "WRITE");
        createPermissionIfNotExists("USER_DELETE", "Delete user data", "USER", "DELETE");

        // Admin permissions
        createPermissionIfNotExists("ADMIN_READ", "Read admin data", "ADMIN", "READ");
        createPermissionIfNotExists("ADMIN_WRITE", "Write admin data", "ADMIN", "WRITE");
        createPermissionIfNotExists("ADMIN_DELETE", "Delete admin data", "ADMIN", "DELETE");

        // System permissions
        createPermissionIfNotExists("SYSTEM_READ", "Read system data", "SYSTEM", "READ");
        createPermissionIfNotExists("SYSTEM_WRITE", "Write system data", "SYSTEM", "WRITE");
        createPermissionIfNotExists("SYSTEM_DELETE", "Delete system data", "SYSTEM", "DELETE");
    }

    private void initializeRoles() {
        // Create USER role
        Role userRole = createRoleIfNotExists("USER", "Default user role with basic permissions");
        Set<Permission> userPermissions = new HashSet<>();
        userPermissions.add(permissionRepository.findByName("USER_READ").orElseThrow());
        userRole.setPermissions(userPermissions);
        roleRepository.save(userRole);

        // Create ADMIN role
        Role adminRole = createRoleIfNotExists("ADMIN", "Administrator role with full permissions");
        Set<Permission> adminPermissions = new HashSet<>();
        Arrays.asList("USER_READ", "USER_WRITE", "USER_DELETE", 
                     "ADMIN_READ", "ADMIN_WRITE", "ADMIN_DELETE",
                     "SYSTEM_READ", "SYSTEM_WRITE", "SYSTEM_DELETE")
                .forEach(permName -> {
                    Permission perm = permissionRepository.findByName(permName).orElseThrow();
                    adminPermissions.add(perm);
                });
        adminRole.setPermissions(adminPermissions);
        roleRepository.save(adminRole);

        // Create MODERATOR role
        Role moderatorRole = createRoleIfNotExists("MODERATOR", "Moderator role with limited admin permissions");
        Set<Permission> moderatorPermissions = new HashSet<>();
        Arrays.asList("USER_READ", "USER_WRITE", "ADMIN_READ")
                .forEach(permName -> {
                    Permission perm = permissionRepository.findByName(permName).orElseThrow();
                    moderatorPermissions.add(perm);
                });
        moderatorRole.setPermissions(moderatorPermissions);
        roleRepository.save(moderatorRole);
    }

    private void initializeDefaultUsers() {
        // Create default admin user if it doesn't exist
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setEnabled(true);
            
            // Assign ADMIN role
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            adminUser.addRole(adminRole);
            
            userRepository.save(adminUser);
            log.info("Default admin user created: admin/admin123");
        }

        // Create default user if it doesn't exist
        if (!userRepository.existsByUsername("user")) {
            User regularUser = new User();
            regularUser.setUsername("user");
            regularUser.setEmail("user@example.com");
            regularUser.setPassword(passwordEncoder.encode("user123"));
            regularUser.setFirstName("Regular");
            regularUser.setLastName("User");
            regularUser.setEnabled(true);
            
            // Assign USER role
            Role userRole = roleRepository.findByName("USER").orElseThrow();
            regularUser.addRole(userRole);
            
            userRepository.save(regularUser);
            log.info("Default user created: user/user123");
        }
    }

    private Permission createPermissionIfNotExists(String name, String description, String resource, String action) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setName(name);
                    permission.setDescription(description);
                    permission.setResource(resource);
                    permission.setAction(action);
                    return permissionRepository.save(permission);
                });
    }

    private Role createRoleIfNotExists(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(description);
                    return roleRepository.save(role);
                });
    }
} 