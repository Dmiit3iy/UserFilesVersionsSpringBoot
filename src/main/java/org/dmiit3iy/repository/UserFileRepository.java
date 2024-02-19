package org.dmiit3iy.repository;

import org.dmiit3iy.model.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFileRepository extends JpaRepository<UserFile, Long> {
    List<UserFile> findUserFilesByUserId(long id);

    UserFile findUserFileByUserIdAndFilename(long id, String filename);
    UserFile findUserFileByFilenameAndUserId(String filename, long id);

}