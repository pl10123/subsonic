/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2009 (C) Sindre Mehus
 */
package net.sourceforge.subsonic.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import net.sourceforge.subsonic.domain.Share;

/**
 * Provides database services for shared media.
 *
 * @author Sindre Mehus
 */
public class ShareDao extends AbstractDao {

    private static final String COLUMNS = "id, name, description, username, created, expires, last_visited, visit_count";

    private ShareRowMapper shareRowMapper = new ShareRowMapper();
    private ShareFileRowMapper shareFileRowMapper = new ShareFileRowMapper();

    /**
     * Creates a new share.
     *
     * @param share The share to create.
     * @return The ID of the newly created share.
     */
    public synchronized int createShare(Share share) {
        String sql = "insert into share (" + COLUMNS + ") values (" + questionMarks(COLUMNS) + ")";
        update(sql, null, share.getName(), share.getDescription(), share.getUsername(), share.getCreated(),
                share.getExpires(), share.getLastVisited(), share.getVisitCount());

        return getJdbcTemplate().queryForInt("select max(id) from share");
    }

    /**
     * Returns all shares.
     *
     * @return Possibly empty list of all shares.
     */
    public List<Share> getAllShares() {
        String sql = "select " + COLUMNS + " from share";
        return query(sql, shareRowMapper);
    }

    /**
     * Updates the given share.
     *
     * @param share The share to update.
     */
    public void updateShare(Share share) {
        String sql = "update share set name=?, description=?, username=?, created=?, expires=?, last_visited=?, visit_count=? where id=?";
        update(sql, share.getName(), share.getDescription(), share.getUsername(), share.getCreated(), share.getExpires(),
                share.getLastVisited(), share.getVisitCount(), share.getId());
    }

    /**
     * Creates shared files.
     *
     * @param shareId The share ID.
     * @param paths   Paths of the files to share.
     */
    public void createSharedFiles(int shareId, List<String> paths) {
        String sql = "insert into share_file (share_id, path) values (?, ?)";
        for (String path : paths) {
            update(sql, shareId, path);
        }
    }

    /**
     * Returns files for a share.
     *
     * @param shareId The ID of the share.
     * @return The paths of the shared files.
     */
    public List<String> getSharedFiles(int shareId) {
        return query("select path from share_file where share_id=?", shareFileRowMapper, shareId);
    }

    private static class ShareRowMapper implements ParameterizedRowMapper<Share> {
        public Share mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Share(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getTimestamp(5),
                    rs.getTimestamp(6), rs.getTimestamp(7), rs.getInt(8));
        }
    }

    private static class ShareFileRowMapper implements ParameterizedRowMapper<String> {
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString(1);
        }

    }
}