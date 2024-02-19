package org.dmiit3iy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
@Table(name = "user_files", uniqueConstraints = {@UniqueConstraint(columnNames = {"filename","user_id"})})
@NoArgsConstructor
@AllArgsConstructor
public class UserFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    private String filename;
    @Column
    private String serverFilename;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @JsonIgnore
    private User user;
    }
