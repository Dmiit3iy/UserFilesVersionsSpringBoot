package org.dmiit3iy.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    @NonNull
    private String fio;
    @Column(nullable = false, unique = true)
    @NonNull
    private String login;
    @Column(nullable = false)
    @NonNull
    private String password;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    @Cascade(value = org.hibernate.annotations.CascadeType.DELETE)
    @ToString.Exclude
    @JsonIgnore
    private List<UserFile> userFileList;
}
